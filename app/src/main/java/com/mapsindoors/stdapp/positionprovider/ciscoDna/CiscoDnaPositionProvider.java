package com.mapsindoors.stdapp.positionprovider.ciscoDna;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mapsindoors.livesdk.CiscoDNATopic;
import com.mapsindoors.livesdk.LiveDataManager;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.ReadyListener;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.stdapp.positionprovider.AppPositionProvider;
import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This class provides an implementation of a position provider, relying on the
 * Cisco DNA API, allowing for positioning over Cisco WiFi equipment.
 * Note that the Cisco DNA API requires certain network conditions!
 * Thus, this provider should never be used as the sole position provider.
 */
public class CiscoDnaPositionProvider extends AppPositionProvider {

    private static final String TAG = CiscoDnaPositionProvider.class.getSimpleName();
    public static final String MAPSINDOORS_CISCO_ENDPOINT = "https://ciscodna.mapsindoors.com/";

    private Context mContext;
    private String mWan;
    private String mLan;
    private String mTenantId;
    private String mCiscoDeviceId;
    private CiscoDNATopic mTopic;
    private long mLastTimePositionUpdated;

    private final static long MAX_TIME_SINCE_UPDATE = 1000 * 60 * 5; // 5 minute timeout

    private boolean mIsSubscribed;

    private static final String[] REQUIRED_PERMISSIONS = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};

    /**
     * Constructor, initializes the instance and wires up
     * the broadcast receiver, to listen to network changes.
     * @param context
     */
    public CiscoDnaPositionProvider(@NonNull Context context, @Nullable Map<String, Object> config){
        super(context);
        mContext = context;
        mIsRunning = false;
        mTenantId = (String) config.get("ciscoDnaSpaceTenantId");

        LiveDataManager.getInstance().setOnTopicUnsubscribedListener(topic -> {
            if (mTopic != null) {
                if(topic.matchesCriteria(mTopic)){
                    mIsSubscribed = false;
                    mCiscoDeviceId = null;
                    mIsIPSEnabled = false;
                }
            }
        });

        LiveDataManager.getInstance().setOnTopicSubscribeErrorListener((error, topic) -> {
            if (mTopic != null) {
                if(topic.matchesCriteria(mTopic)){
                    mIsSubscribed = false;
                    mCiscoDeviceId = null;
                    mIsIPSEnabled = false;
                }
            }
        });

        LiveDataManager.getInstance().setOnErrorListener(error -> {
            if (error.code == MIError.LIVEDATA_CONNECTION_FAILED || error.code == MIError.LIVEDATA_CONNECTION_LOST || error.code == MIError.LIVEDATA_STATE_NETWORK_FAILURE) {
                mIsSubscribed = false;
                mCiscoDeviceId = null;
                mIsIPSEnabled = false;
            }
        });

        LiveDataManager.getInstance().setOnReceivedLiveUpdateListener((topic, message) -> {
            if(message.getId().equals(mCiscoDeviceId)){
                mLatestPosition = message.getPositionResult();
                mLastTimePositionUpdated = System.currentTimeMillis();
                reportPositionUpdate();
            }
        });

    }

    private void startSubscription(){
        mTopic = new CiscoDNATopic(mTenantId, mCiscoDeviceId);

        if(!mIsSubscribed){
            LiveDataManager.getInstance().setOnTopicSubscribedListener(topic -> {
                if(topic.equals(mTopic)){
                    mCanDeliver = true;
                    mIsIPSEnabled = true;
                    mIsSubscribed = true;
                }
            });
            LiveDataManager.getInstance().subscribeTopic(mTopic);
        }
    }

    private void unsubscribe(){
        LiveDataManager.getInstance().unsubscribeTopic(mTopic);
    }


    private void update(ReadyListener listener){
        if(!isOnline()){
            mCanDeliver = false;
            return;
        }

        updateAddressesAndId(() -> {
            if(mCiscoDeviceId != null && !mCiscoDeviceId.isEmpty()){
                mCanDeliver = true;
                if(mLatestPosition == null){
                    obtainInitialPosition(listener);
                }
            }
            listener.onResult();
        });
    }

    private void obtainInitialPosition(ReadyListener listener){
        final String url = MAPSINDOORS_CISCO_ENDPOINT + mTenantId + "/api/ciscodna/" + mCiscoDeviceId;
        Request request = new Request.Builder().url(url).build();
        if (mCiscoDeviceId != null && mTenantId != null) {
            OkHttpClient httpClient = new OkHttpClient();
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    listener.onResult();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if(response.code() == HttpURLConnection.HTTP_NOT_FOUND){
                        listener.onResult();
                        return;
                    }

                    String json = response.body().string();
                    CiscoDNAEntry positionResult = new Gson().fromJson(json, CiscoDNAEntry.class);

                    mLatestPosition = positionResult;
                    mLastTimePositionUpdated = System.currentTimeMillis();
                    reportPositionUpdate();

                    listener.onResult();
                }
            });
        }
    }

    @NonNull
    @Override
    public String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    @Override
    public boolean isPSEnabled() {
        return mIsRunning && mLatestPosition != null;
    }

    @Override
    public void startPositioning(@Nullable String s) {
        if(!mIsRunning){
            mIsRunning = true;
            update(this::startSubscription);
        }
    }

    @Override
    public void stopPositioning(@Nullable String s) {
        if(mIsRunning){
            mIsRunning = false;
            unsubscribe();
        }
    }

    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void checkPermissionsAndPSEnabled(@Nullable PermissionsAndPSListener permissionAndPSlistener) {
        if (mContext != null) {
            PSUtils.checkLocationPermissionAndServicesEnabled(getRequiredPermissions(), mContext, permissionAndPSlistener);
        }
    }

    @Nullable
    @Override
    public String getProviderId() {
        return mProviderId;
    }

    @Nullable
    @Override
    public PositionResult getLatestPosition() {
        return null;
    }

    @Override
    public void startPositioningAfter(int i, @Nullable String s) { }

    @Override
    public void terminate() {
        stopPositioning( null );
        mContext = null;
    }

    /**
     * Check if the device currently has online connectivity
     * @return boolean
     */
    private boolean isOnline() {
        if (mContext == null) {
            return false;
        }
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
        //mCiscoDeviceId = null;
        fetchExternalAddress(() -> {
            if(mTenantId != null && mLan != null && mWan != null){
                String url = MAPSINDOORS_CISCO_ENDPOINT + mTenantId + "/api/ciscodna/devicelookup?clientIp=" + mLan + "&wanIp=" + mWan;
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
                        Log.d("ciscodnaprovider", "Could not obtain deviceId from backend deviceID request! Code: " + Integer.toString(response.code()));
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

    @Nullable
    private String getLocalAddress(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipv4 = inetAddress.getHostAddress().toString();
                        return ipv4;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(this.getClass().getSimpleName(), "Failed to resolve LAN address");
        }

        return null;
    }

    private void fetchExternalAddress(@NonNull ReadyListener listener){
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url("https://ipinfo.io/ip").build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                listener.onResult();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String str = response.body().string();
                    mWan = str;
                }
                listener.onResult();
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
        update(() -> {
            if(mCiscoDeviceId != null){
                if(!mIsSubscribed){
                    startSubscription();
                }
            }

            onComplete.onResult();
        });
    }

    @Override
    protected boolean getCanDeliver() {
        return mLatestPosition != null &&
                mIsSubscribed &&
                mLastTimePositionUpdated + MAX_TIME_SINCE_UPDATE > System.currentTimeMillis() &&
                isOnline();
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
        return "1.0.0";
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

        if(mLatestPosition != null){
            String k = mLatestPosition.toString();
            strBuilder.append(k);
            strBuilder.append("\n");
        }

        return strBuilder.toString();
    }


}
