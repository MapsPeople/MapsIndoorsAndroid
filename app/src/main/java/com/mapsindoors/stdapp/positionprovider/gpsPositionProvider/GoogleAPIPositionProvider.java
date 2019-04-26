package com.mapsindoors.stdapp.positionprovider.gpsPositionProvider;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.mapsindoors.mapssdk.MPPositionResult;
import com.mapsindoors.mapssdk.OnPositionUpdateListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.broadcastReceivers.GPSStateChangeReceiver;
import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


/**
 *
 * 
 *
 * @see <a href="https://developers.google.com/android/guides/releases"></a>
 * @see <a href="https://developers.google.com/android/guides/api-client"></a>
 *
 * @see <a href="https://developer.android.com/training/location/retrieve-current.html">Getting the Last Known Location</a>
 * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient">FusedLocationProviderClient</a>
 */
public class GoogleAPIPositionProvider extends Activity
        implements PositionProvider,
            com.google.android.gms.location.LocationListener,
            com.google.android.gms.maps.LocationSource,
            android.location.LocationListener
{
    public static final String TAG = GoogleAPIPositionProvider.class.getSimpleName();


    /** How much the user needs to move before reporting a position update, in meters */
    private static final double UPDATE_DISTANCE_THRESHOLD = 1.0;
    private static final double UPDATE_ALTITUDE_THRESHOLD = 2.0;

    /**  */
    private static final String[] REQUIRED_PERMISSIONS = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};


    private Context mContext;

    private List<OnPositionUpdateListener> mPositionUpdateListeners;
    private String mProviderId;
    private PositionResult mLatestPosition;
    private boolean mIsRunning;
    private boolean mIsWaitingForDelayedPositioningStart;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private List<OnStateChangedListener> mStateChangedlistenersList;
    GPSStateChangeReceiver mGPSStateChangeReceiver;


    public GoogleAPIPositionProvider( Context context ) {
        super();

        mStateChangedlistenersList = new ArrayList<>();

        mContext = context;

        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        mIsRunning =
        mIsWaitingForDelayedPositioningStart = false;

        {
            // Register broadcast receivers
            mGPSStateChangeReceiver = new GPSStateChangeReceiver(mContext);

            GPSStateChangeReceiver.addOnStateChangedListener( this::reportStateToListeners );

            mContext.registerReceiver(mGPSStateChangeReceiver, new IntentFilter( "android.location.PROVIDERS_CHANGED" ) );
        }
    }


    void reportStateToListeners( boolean state ) {
        if( !mStateChangedlistenersList.isEmpty() ) {
            for(int i = 0, aLen=mStateChangedlistenersList.size(); i<aLen; i++){
                if(PSUtils.arePermissionsGranted(mContext, getRequiredPermissions())){
                    mStateChangedlistenersList.get(i).onStateChanged( state );
                }
                else {
                    mStateChangedlistenersList.get(i).onStateChanged( false );
                }
            }

        }
    }



    //region Implements Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        terminate();
        super.onDestroy();
    }
    //endregion


    //region Implements PositionProvider
    @NonNull
    @Override
    public String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    @Override
    public boolean isPSEnabled() {
        return (PSUtils.isLocationPermissionGranted(mContext) && PSUtils.isLocationServiceEnabled(mContext));
    }



    @Override
    public void startPositioning( String arg ) {
        if (!mIsRunning ) {

            mIsRunning = true;
            mIsWaitingForDelayedPositioningStart = false;

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder( mContext ).
                        addConnectionCallbacks( new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected( @Nullable Bundle bundle ) {
                                requestLocationStart();
                            }

                            @Override
                            public void onConnectionSuspended( int cause ) {
                                if( cause == CAUSE_SERVICE_DISCONNECTED ) {
                                    if( dbglog.isDebugMode() ) {
                                        dbglog.Log( TAG, "Disconnected. Please re-connect." );
                                    }
                                } else if( cause == CAUSE_NETWORK_LOST ) {
                                    if( dbglog.isDebugMode() ) {
                                        dbglog.Log( TAG, "Network lost. Please re-connect." );
                                    }
                                }
                            }
                        }).
                        addOnConnectionFailedListener( connectionResult -> {
                            if( dbglog.isDebugMode() ) {
                                dbglog.Log( TAG, "Failed to connect to LocationServices. Error (1) code: " + connectionResult.getErrorMessage() + " / " + connectionResult.toString() );
                            }
                        } ).
                        addApi( com.google.android.gms.location.LocationServices.API ).
                        build();
            }

            mGoogleApiClient.connect();

            if ( mPositionUpdateListeners != null) {
                if( dbglog.isDebugMode() ) {
                    dbglog.Log( TAG, "startPositioning( " + arg + " ) - Start" );
                }

                for (OnPositionUpdateListener listener : mPositionUpdateListeners ) {
                    if( listener != null) {
                        listener.onPositioningStarted( this );
                    }
                }
            }
        }
    }

    @Override
    public void stopPositioning(String arg) {

        if (dbglog.isDebugMode()) {
            dbglog.Log( TAG, "stopPositioning( " + arg + " ) - Start" );
        }

        if ( mIsRunning ) {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.disconnect();
            }
            mIsRunning =
            mIsWaitingForDelayedPositioningStart = false;
        }
    }

    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void addOnPositionUpdateListener( @Nullable OnPositionUpdateListener listener ) {
        if( listener != null ) {
            if( mPositionUpdateListeners == null ) {
                mPositionUpdateListeners = new ArrayList<>();
            }

            mPositionUpdateListeners.remove( listener );
            mPositionUpdateListeners.add( listener );
        }
    }

    @Override
    public void removeOnPositionUpdateListener( @Nullable OnPositionUpdateListener listener ) {
        if( listener != null ) {
            if( mPositionUpdateListeners != null ) {
                mPositionUpdateListeners.remove( listener );
                if( mPositionUpdateListeners.isEmpty() ) {
                    mPositionUpdateListeners = null;
                }
            }
        }
    }

    @Override
    public void setProviderId(String id) {
        mProviderId = id;
    }

    @Override
    public void addOnstateChangedListener(OnStateChangedListener onStateChangedListener) {
        mStateChangedlistenersList.add(onStateChangedListener);
    }

    @Override
    public void removeOnstateChangedListener(OnStateChangedListener onStateChangedListener) {
        mStateChangedlistenersList.remove(onStateChangedListener);
    }

    @Override
    public void checkPermissionsAndPSEnabled(PermissionsAndPSListener permissionsAndPSListener) {
        PSUtils.checkLocationPermissionAndServicesEnabled(getRequiredPermissions(),mContext,permissionsAndPSListener);
    }

    @Override
    public String getProviderId() {
        return mProviderId;
    }

    @Override
    @Nullable
    public PositionResult getLatestPosition() {
        return mLatestPosition;
    }

    @Override
    public void startPositioningAfter( @IntRange(from = 0, to = Integer.MAX_VALUE) int delayInMs, final String arg ) {
        if( !mIsWaitingForDelayedPositioningStart )
        {
            mIsWaitingForDelayedPositioningStart = true;

            Timer restartTimer = new Timer();
            restartTimer.schedule( new TimerTask() {
                @Override
                public void run()
                {
                    startPositioning( arg );
                }
            }, delayInMs );
        }
    }

    @Override
    public void terminate()
    {
        if( mGoogleApiClient != null )
        {
            if( mGoogleApiClient.isConnected() )
            {
                mGoogleApiClient.disconnect();
            }
            mGoogleApiClient = null;
        }

        if( mContext != null && mGPSStateChangeReceiver != null )
        {
            mContext.unregisterReceiver(mGPSStateChangeReceiver);

            mGPSStateChangeReceiver.terminate();
        }

        mContext = null;
    }
    //endregion


    //region Implements LocationListener
    @Override
    public void onLocationChanged( @Nullable Location location ) {
        if (location == null) {
            mLatestPosition = null;
            return;
        }

        if( isRunning() ) {

            MPPositionResult newLocation = new MPPositionResult(
                    new Point( location ),
                    location.getAccuracy(),
                    location.getBearing()
            );

            // From Google's Santa tracker:
            // "Update our current location only if we've moved at least a metre, to avoid
            // jitter due to lack of accuracy in FusedLocationApi"
            if( mLatestPosition != null ) {

                // Check the distance between the prev and new position in 2D (lat/lng)
                final double dist = mLatestPosition.getPoint().distanceTo( newLocation.getPoint() );
                if( dist <= UPDATE_DISTANCE_THRESHOLD ) {

                    // Get the altitude too. Just imagine the lady/guy is using a lift/elevator/"spiral staircase"...
                    // Use the prev position "android location object" altitude value to run the check
                    android.location.Location prevLocation = mLatestPosition.getAndroidLocation();
                    if( prevLocation != null )
                    {
                        final double altDiff = Math.abs( location.getAltitude() - prevLocation.getAltitude() );
                        if( altDiff <= UPDATE_ALTITUDE_THRESHOLD )
                        {
                            if( BuildConfig.DEBUG ){}
                            return;
                        }
                    }
                }
            }

            mLatestPosition = newLocation;
            mLatestPosition.setProvider( this );
            mLatestPosition.setAndroidLocation( location );

            if( mPositionUpdateListeners != null ) {
                for( OnPositionUpdateListener listener : mPositionUpdateListeners ) {
                    if( listener != null ) {
                        listener.onPositionUpdate( mLatestPosition );
                    }
                }
            }
        }
    }
    //endregion


    //region Implements LocationSource

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        if (dbglog.isDebugMode()) {
            dbglog.Log(TAG, "activate()");
        }
    }

    @Override
    public void deactivate() {
        if (dbglog.isDebugMode()) {
            dbglog.Log(TAG, "deactivate()");
        }
    }
    //endregion


    //region Implements android.location.LocationListener
    //endregion


    void requestLocationStart() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval( 5000 );
        mLocationRequest.setFastestInterval( 2000 );
        mLocationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().
                addLocationRequest( mLocationRequest );
        final LocationListener locationListener = this;

        final PendingResult< LocationSettingsResult > result = LocationServices.SettingsApi.checkLocationSettings( mGoogleApiClient, builder.build() );

        result.setResultCallback( locationSettingsResult -> {

            int statusCode = locationSettingsResult.getStatus().getStatusCode();

            if( statusCode == LocationSettingsStatusCodes.SUCCESS ) {
                try {
                    // "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"
                    LocationServices.FusedLocationApi.requestLocationUpdates( mGoogleApiClient, mLocationRequest, locationListener );

                } catch( SecurityException ex ) {

                    stopPositioning( null );

                    if( dbglog.isDebugMode() ) {
                        dbglog.Log( TAG, "GPS Permission denied : " + ex.getMessage() );
                    }
                }

            } else {
                if( dbglog.isDebugMode() ) {
                    dbglog.Log( TAG, "Failed to connect to LocationServices. Using the native LocationManager as fallback. Status code: " + LocationSettingsStatusCodes.getStatusCodeString( statusCode ) );
                }

                requestLocationStartLow();
            }
        } );

    }

   public void requestLocationStartLow() {
        // to avoid app crash when the gps access is not granted
        try {
            // "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);

            onLocationChanged( mLocationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER ) );

        } catch (SecurityException ex) {

            // This exception fire up when the gps access is not granted
            stopPositioning("");

            if (dbglog.isDebugMode()) {
                dbglog.Log(TAG, "GPS Permission denied : " + ex.getMessage());
            }
        }
    }


    //region android.location.LocationListener methods
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        if (dbglog.isDebugMode()) {
            dbglog.Log(TAG, String.format(Locale.US, "onStatusChanged - s: %s, i: %d", s, i));
        }
    }

    long lastTimeIWasCalled=0;
    @Override
    public void onProviderEnabled(String s) {
        if (dbglog.isDebugMode()) {
            long now = System.currentTimeMillis();
            dbglog.Log(TAG, String.format(Locale.US, "onProviderEnabled - s: %s, dt: %d", s, (int)(now-lastTimeIWasCalled)) );
            lastTimeIWasCalled = now;
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if (dbglog.isDebugMode()) {
            dbglog.Log(TAG, String.format(Locale.US, "onProviderDisabled - s: %s", s));
        }
    }

}
