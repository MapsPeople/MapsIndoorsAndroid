package com.mapsindoors.stdapp.positionprovider.ciscoDna;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.ReadyListener;
import com.mapsindoors.stdapp.helpers.UrlClient;
import com.mapsindoors.stdapp.positionprovider.AppPositionProvider;
import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.WIFI_SERVICE;

/**
 * This class provides an implementation of a position provider, relying on the
 * Cisco DNA API, allowing for positioning over Cisco WiFi equipment.
 * Note that the Cisco DNA API requires certain network conditions!
 * Thus, this provider should never be used as the sole position provider.
 */
public class CiscoDnaPositionProvider extends AppPositionProvider {

    public static final String MAPSINDOORS_CISCO_ENDPOINT = "https://ciscodna.mapsindoors.com/";

    // How often we poll the backend for location data (in milliseconds)
    private static final int POLLING_INTERVAL = 1000;
    private static final String[] REQUIRED_PERMISSIONS = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};

    private Context mContext;
    private String mWan;
    private String mLan;
    private String mTenantId;
    private String mCiscoDeviceId;
    private Handler mQueryLocationTaskHandler;
    private CiscoDNAEntry mLatestCiscoPosition;
    private boolean mIsWaitingForDelayedPositioningStart;

    /**
     * Constructor, initializes the instance and wires up
     * the broadcast receiver, to listen to network changes.
     * @param context
     */
    public CiscoDnaPositionProvider(@NonNull Context context, @Nullable Map<String, Object> config){
        super(context);
        mContext = context;
        mIsRunning = false;
        mIsWaitingForDelayedPositioningStart = false;
        mQueryLocationTaskHandler = new Handler();

        mTenantId = (String) config.get("ciscoDnaSpaceTenantId");

        mLan = getLocalAddress();
        fetchExternalAddress(null);
    }

    /**
     * Acquires the local ip address, on the current wifi network.
     * If the device is connected to cellular network, or offline, this address
     * evaluates to 0.0.0.0
     * @return string containing local ip
     */
    private String getLocalAddress(){
        //TODO: Rewrite to use not use deprecated formatIpAddress(supports only ipv4)
        if (mContext.getApplicationContext() != null) {
            WifiManager wm = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            return ip;
        }
        return null;
    }

    private void fetchExternalAddress(ReadyListener listener){
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://ipinfo.io/ip")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) { }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String str = response.body().string();
                    mWan = str;
                }
                if(listener != null){
                    listener.onResult();
                }
            }
        });
    }

    /**
     * Defines a runnable object, for sending update requests to the backend.
     * This runnable is executed at every POLLING_INTERVAL (1 second), and updates
     * the latest position, if the backend delivers newer data than what we currently have.
     */
    private Runnable pingBackendForLocation = new Runnable() {
        @Override
        public void run() {
            // TODO Should be moved into a sepearate method
            String url = MAPSINDOORS_CISCO_ENDPOINT + mTenantId + "/api/ciscodna/" + mCiscoDeviceId;
            Request request = new Request.Builder().url(url).build();
            if (mIsRunning && mCiscoDeviceId != null && mTenantId != null) {
                UrlClient<CiscoDNAEntry> ciscoDnaEntryClient = new UrlClient<>(request, CiscoDNAEntry.class, result -> {
                    if(result != null){
                        // Check if the new location has a newer timestamp than the current location state
                        if(mLatestCiscoPosition != null){
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ROOT);
                            Date newStateTime = null;
                            Date currentStateTime = null;
                            try {
                                newStateTime = dateFormat.parse(result.getTimestamp());
                                if(mLatestCiscoPosition.getTimestamp() != null){
                                    currentStateTime = dateFormat.parse(mLatestCiscoPosition.getTimestamp());
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return;
                            }

                            if(newStateTime == null) {
                                return;
                            }

                            if(currentStateTime == null || currentStateTime.compareTo(newStateTime) < 0){
                                mLatestCiscoPosition = result;
                                mLatestPosition = mLatestCiscoPosition;
                            }
                            reportPositionUpdate();
                        }
                        else {
                            mLatestCiscoPosition = result;
                            mLatestPosition = mLatestCiscoPosition;
                            reportPositionUpdate();
                        }
                    }
                });
                ciscoDnaEntryClient.execute();
            }

            mQueryLocationTaskHandler.postDelayed(this, POLLING_INTERVAL);
        }
    };

    @NonNull
    @Override
    public String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    @Override
    public boolean isPSEnabled() {
        return mIsRunning && mLatestCiscoPosition != null;
    }

    @Override
    public void startPositioning(@Nullable String s) {
        if(!mIsRunning){
            mIsRunning = true;
            mIsWaitingForDelayedPositioningStart = false;
            mQueryLocationTaskHandler.postDelayed(pingBackendForLocation, POLLING_INTERVAL);
        }
    }

    @Override
    public void stopPositioning(@Nullable String s) {
        if(mIsRunning){
            mIsRunning = false;
            mIsWaitingForDelayedPositioningStart = false;
            mQueryLocationTaskHandler.removeCallbacks(pingBackendForLocation);
        }
    }

    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void checkPermissionsAndPSEnabled(@Nullable PermissionsAndPSListener permissionAndPSlistener) {
        PSUtils.checkLocationPermissionAndServicesEnabled(getRequiredPermissions(), mContext, permissionAndPSlistener);
    }

    @Nullable
    @Override
    public String getProviderId() {
        return mProviderId;
    }

    @Nullable
    @Override
    public PositionResult getLatestPosition() {
        return mLatestCiscoPosition;
    }

    @Override
    public void startPositioningAfter(int i, @Nullable String s) {
        if( !mIsWaitingForDelayedPositioningStart ) {
            mIsWaitingForDelayedPositioningStart = true;
            mQueryLocationTaskHandler.postDelayed(() -> startPositioning(null), i);
        }
    }

    @Override
    public void terminate() {
        stopPositioning( null );
        mLatestCiscoPosition = null;
        mIsRunning = false;
        mQueryLocationTaskHandler.removeCallbacks(pingBackendForLocation);
        mContext = null;
    }

    /**
     * Check if the device currently has online connectivity
     * @return boolean
     */
    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * This method is responsible for gathering the local and external IP addresses
     * as well as acquiring a device ID from the Cisco DNA API.
     */
    private void updateAddressesAndId(ReadyListener onComplete) {
        mLan = getLocalAddress();
        mCiscoDeviceId = null;

        // Upon getting the WAN address, go ahead and try to get the deviceId
        // Upon completion, the original listener is called
        fetchExternalAddress(() -> {
            if(mTenantId != null && mLan != null && mWan != null){
                String url = MAPSINDOORS_CISCO_ENDPOINT + mTenantId + "/api/ciscodna/devicelookup?clientIp="+mLan+"&wanIp="+mWan;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Gson gson = new Gson();
                        String json = response.body().string();
                        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                        mCiscoDeviceId = jsonObject.get("deviceId").getAsString();
                    } else {
                        Log.d("ciscodnaprovider", "Could not obtain deviceId from backend deviceID request! Code: " + response.code());
                    }
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(onComplete != null){
                onComplete.onResult();
            }
        });
    }

    /**
     * Check whether or not a position provider instance is capable of
     * providing positioning data, under current conditions.
     *
     * @return true or false,
     */
    @Override
    public void checkIfCanDeliver(ReadyListener onComplete) {
        updateAddressesAndId(() -> {
            if(mCiscoDeviceId != null && !mCiscoDeviceId.isEmpty() &&
                    !mWan.isEmpty() &&
                    !mWan.equals("0.0.0.0") &&
                    !mLan.equals("0.0.0.0")){
                Log.d("ciscodnaprovider", "Cisco DNA provider can deliver!");
                mCanDeliver = true;
            }
            else{
                Log.d("ciscodnaprovider", "Cisco DNA provider can NOT deliver!");
                mCanDeliver = false;
            }
            // Report back, that the "can deliver?" check has been performed
            if(onComplete != null){
                onComplete.onResult();
            }
        });

        if(!isOnline()){
            mCanDeliver = false;
            onComplete.onResult();
        }
    }

    @Override
    protected boolean getCanDeliver() {
        Log.d("ciscodnaprovider", "Has internalIP: " + mLan);
        Log.d("ciscodnaprovider", "Has externalIP: " + mWan);
        Log.d("ciscodnaprovider", "Has CiscoId: " + mCiscoDeviceId);
        return mCanDeliver;
    }

    @Override
    public String getName() {
        return "CiscoDNA";
    }

    /**
     * Returns the versioning of the position provider
     *
     * @return version name string
     */
    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public String getAdditionalMetaData() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("DeviceId: ");
        strBuilder.append(mCiscoDeviceId);
        strBuilder.append("\n");

        strBuilder.append("Wan: ");
        strBuilder.append(mWan);
        strBuilder.append("Lan: ");
        strBuilder.append(mLan);
        strBuilder.append("\n");

        if(mLatestCiscoPosition != null){
            String k = mLatestCiscoPosition.toString();
            strBuilder.append(k);
            strBuilder.append("\n");
        }

        return strBuilder.toString();
    }
}
