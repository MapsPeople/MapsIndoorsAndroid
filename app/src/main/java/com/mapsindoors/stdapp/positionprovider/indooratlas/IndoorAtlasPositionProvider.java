package com.mapsindoors.stdapp.positionprovider.indooratlas;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.internal.LinkedTreeMap;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IAOrientationListener;
import com.indooratlas.android.sdk.IAOrientationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.mapsindoors.mapssdk.Floor;
import com.mapsindoors.mapssdk.MPPositionResult;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.ReadyListener;
import com.mapsindoors.stdapp.positionprovider.AppPositionProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Position provider implementation for IndoorAtlas
 */
public class IndoorAtlasPositionProvider extends AppPositionProvider {

    private static final long MIN_TIME_BETWEEN_UPDATES_IN_MS = 100;

    private IALocationManager mIndoorAtlasClient;
    private IARegion mIACurrentVenue;
    private IARegion mIACurrentFloorPlan;
    private Integer mIACurrentFloorLevel;
    private Float mIACurrentCertainty;

    private long mLastHeadingUpdateTime;
    private long mLastOrientationUpdateTime;
    private float mLatestBearing;

    private Map<Integer, Integer> mFloorMapping;

    private boolean mCanDeliver;

    private String apiKey;
    private String secret;

    private Map<String, Object> mConfig;
    private Context mContext;

    public IndoorAtlasPositionProvider(@NonNull Context context, Map<String, Object> config) {
        super(context);

        mContext = context;
        mConfig = config;
        initClient();

        if (Build.VERSION.SDK_INT >= 31) {
            REQUIRED_PERMISSIONS = new String[]{
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.BLUETOOTH_SCAN"
            };
        }else {
            REQUIRED_PERMISSIONS = new String[]{
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.BLUETOOTH_ADMIN",
                    "android.permission.BLUETOOTH"
            };
        }
    }

    private void initClient(){
        apiKey = (String) mConfig.get("key");
        secret = (String) mConfig.get("secret");

        mFloorMapping = constructFloorMapping(mConfig);

        if(apiKey == null || TextUtils.isEmpty(apiKey)|| secret == null || TextUtils.isEmpty(secret) || mFloorMapping.isEmpty()){
            Log.e(this.getClass().getSimpleName(), "IndoorAtlas API key/secret is either null or empty string, or floor mapping is missing!");
            mCanDeliver = false;
        } else {
            mCanDeliver = true;
        }

        Bundle extras = new Bundle(2);
        extras.putString(IALocationManager.EXTRA_API_KEY, apiKey);
        extras.putString(IALocationManager.EXTRA_API_SECRET, secret);

        mIndoorAtlasClient = IALocationManager.create(mContext, extras);
        mIndoorAtlasClient.registerRegionListener(regionListener);
        mIndoorAtlasClient.registerOrientationListener(new IAOrientationRequest( 1, 0 ), mOrientationListener);

        // Enable switching to GPS when outside, in the IndoorAtlas SDK
        mIndoorAtlasClient.lockIndoors(false);
    }

    /**
     * Creates an int:int map, from the floor mapping from the backend
     * @param config
     * @return
     */
    private Map<Integer, Integer> constructFloorMapping(Map<String, Object> config){
        Map<Integer, Integer> floorMapping = new HashMap<>();

        Object mappingObject = config.get("floorMapping");
        if(mappingObject != null){
            LinkedTreeMap<String, Double> map = (LinkedTreeMap<String, Double>) mappingObject;

            // Convert to int:int map
            for(Map.Entry<String, Double> entry : map.entrySet()){
                int key = Integer.parseInt(entry.getKey());
                double val = entry.getValue();
                floorMapping.put(key, (int)val);
            }
        }

        return floorMapping;
    }

    @Override
    public void startPositioning( @Nullable final String arg ) {
        if(!mIsRunning){
            initClient();
            mIndoorAtlasClient.requestLocationUpdates( IALocationRequest.create(), locationListener );
            mIsRunning = true;
        }
    }

    @Override
    public void stopPositioning( @Nullable final String arg ) {
        if(mIsRunning && mIndoorAtlasClient != null) {
            mIndoorAtlasClient.unregisterOrientationListener(mOrientationListener);
            mIndoorAtlasClient.unregisterRegionListener(regionListener);
            mIndoorAtlasClient.removeLocationUpdates(locationListener);
            mIndoorAtlasClient.lockIndoors(true);
            mIsRunning = false;
        }
    }

    // CHANGE THE INTERFACE TO NOT HAVE THIS NONSENSE
    @Override
    public void startPositioningAfter(int delayInMs, @Nullable String mapsIndoorsKey) { }

    @Override
    public void terminate() {
        if( mIndoorAtlasClient != null ) {
            mIndoorAtlasClient.destroy();
            mIndoorAtlasClient = null;
        }
    }

    private final IALocationListener locationListener = new IALocationListener() {
        @Override
        public void onLocationChanged( @Nullable final IALocation location ) {
            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();
            final double altitude = location.getAltitude();
            final int floorLevel = location.getFloorLevel();
            final float floorCertainty = location.getFloorCertainty();
            final float accuracy = location.getAccuracy();
            final float bearing = location.getBearing();
            final long time = location.getTime();
            final IARegion region = location.getRegion();

            final boolean hasFloorLevel = location.hasFloorLevel();
            final boolean hasFloorCertainty = location.hasFloorCertainty();

            mIACurrentFloorLevel = hasFloorLevel ? floorLevel : null;
            mIACurrentCertainty = hasFloorCertainty ? floorCertainty : null;

            if( isRunning() ) {
                mIsIPSEnabled = true;

                final MPPositionResult newLocation = new MPPositionResult( new Point( latitude, longitude ), accuracy, mLatestBearing);
                newLocation.setAndroidLocation( location.toLocation() );
                mLatestPosition = newLocation;

                if( hasFloorLevel ) {
                    final int miFloorIndex;

                    if( mFloorMapping.containsKey(floorLevel) ) {
                        miFloorIndex = mFloorMapping.get(floorLevel);
                    } else {
                        miFloorIndex = Floor.DEFAULT_GROUND_FLOOR_INDEX;
                    }

                    mLatestPosition.setFloor( miFloorIndex );
                } else {
                    mLatestPosition.setFloor( Floor.DEFAULT_GROUND_FLOOR_INDEX );
                }

                mLatestPosition.setProvider( IndoorAtlasPositionProvider.this );
                reportPositionUpdate();
            }
        }

        @Override
        public void onStatusChanged(@Nullable final String provider, final int status, @Nullable final Bundle extras ) {
            if(status == IALocationManager.STATUS_TEMPORARILY_UNAVAILABLE || status == IALocationManager.STATUS_OUT_OF_SERVICE){
                reportStateChanged(false);
            }
        }
    };

    private IAOrientationListener mOrientationListener = new IAOrientationListener() {
        @Override
        public void onHeadingChanged( long timestamp, double heading ) {
            if( mLatestPosition != null ) {
                final long dt = timestamp - mLastHeadingUpdateTime;

                if( dt < MIN_TIME_BETWEEN_UPDATES_IN_MS ) {
                    return;
                }

                mLastHeadingUpdateTime = timestamp;

                final float bearing = (float) heading;

                mLatestPosition.setBearing( bearing );
                mLatestBearing = bearing;

                reportPositionUpdate();
            }
        }

        @Override
        public void onOrientationChange( long timestamp, @Nullable double[] quaternion ) {
            if( mLatestPosition != null ) {
                final long dt = timestamp - mLastOrientationUpdateTime;

                if( dt < MIN_TIME_BETWEEN_UPDATES_IN_MS ) {
                    return;
                }

                mLastOrientationUpdateTime = timestamp;
            }
        }
    };

    private final IARegion.Listener regionListener = new IARegion.Listener() {
        @Override
        public void onEnterRegion( @Nullable final IARegion region ) {
            IndoorAtlasPositionProvider.this.onEnterRegion( region );
        }

        @Override
        public void onExitRegion( @Nullable final IARegion region ) {
            IndoorAtlasPositionProvider.this.onExitRegion( region );
        }
    };

    private void onEnterRegion(@Nullable final IARegion region) {
        onRegionChanged( region );
    }

    private void onExitRegion(@Nullable final IARegion region) {
        onRegionChanged( region );
    }

    private void onRegionChanged(@Nullable final IARegion region) {
        if( region == null ) {
            return;
        }

        switch( region.getType() ) {
            case IARegion.TYPE_UNKNOWN:
            default:
                break;
            case IARegion.TYPE_FLOOR_PLAN:
                mIACurrentFloorPlan = region;
                break;
            case IARegion.TYPE_VENUE:
                mIACurrentVenue = region;
                break;
        }
    }

    /**
     * Check whether or not a position provider instance is capable of
     * providing positioning data, under current conditions.
     *
     * @return true or false,
     */
    @Override
    protected void checkIfCanDeliver(ReadyListener onComplete) {
        if(isOnline()){
            mCanDeliver = true;
        } else {
            mCanDeliver = false;
        }
        if(onComplete != null){
            onComplete.onResult();
        }
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected boolean getCanDeliver() {
        return mCanDeliver;
    }

    /**
     * Returns the name of the {@link PositionProvider}
     *
     * @return Name of the PositionProvider
     */
    @Override
    public String getName() {
        return "IndoorAtlas";
    }

    /**
     * Returns the versioning of the position provider
     *
     * @return version name string
     */
    @Override
    public String getVersion() {
        if (mIndoorAtlasClient == null) {
            return IALocationManager.create(mContext).getExtraInfo().version;
        } else {
            return mIndoorAtlasClient.getExtraInfo().version;
        }
    }

    @Override
    public String getAdditionalMetaData() {
        return null;
    }

}
