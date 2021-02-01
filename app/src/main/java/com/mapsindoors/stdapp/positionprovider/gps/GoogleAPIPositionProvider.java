package com.mapsindoors.stdapp.positionprovider.gps;

import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.mapsindoors.mapssdk.Floor;
import com.mapsindoors.mapssdk.MPPositionResult;
import com.mapsindoors.mapssdk.OnPositionUpdateListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.ReadyListener;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.broadcastReceivers.GPSStateChangeReceiver;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.listeners.OnBearingChangedListener;
import com.mapsindoors.stdapp.positionprovider.AppPositionProvider;
import com.mapsindoors.stdapp.positionprovider.helpers.CompassBearingProvider;
import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class GoogleAPIPositionProvider extends AppPositionProvider
{
    public static final String TAG = GoogleAPIPositionProvider.class.getSimpleName();


    public static final long  LOCATION_REQUEST_INTERVAL_IN_MS                = 5000;
    public static final long  LOCATION_REQUEST_FASTEST_INTERVAL_IN_MS        = 2000;
    public static final long  LOCATION_UPDATE_REQUEST_MIN_TIME_IN_MS         = 5000;
    public static final float LOCATION_UPDATE_REQUEST_MIN_DISTANCE_IN_METERS = 1;
    public static final float BEARING_REPORT_AS_CHANGED_THRESHOLD            = 6; //3;

    /**
     * How much the user needs to move before reporting a position update, in meters
     */
    private static final double UPDATE_DISTANCE_THRESHOLD = 1.0;
    private static final double UPDATE_ALTITUDE_THRESHOLD = 2.0;

    /**
     *
     */
    private static final String[] REQUIRED_PERMISSIONS = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};


    private Context mContext;
    private PositionProvider mPositionProvider;
    private GPSStateChangeReceiver mGPSStateChangeReceiver;
    private CompassBearingProvider mCompassBearingProvider;

    private String mProviderId;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private Timer restartTimer;
    private List<OnPositionUpdateListener> mPositionUpdateListeners;
    private List<OnStateChangedListener>   mStateChangedListeners;

    private boolean mIsWaitingForDelayedPositioningStart;
    private boolean mIPSEnabled;
    private boolean mEnableCompassBearing;
    private float mLastBearingDegree;


    public GoogleAPIPositionProvider( @NonNull Context context ) {
        super(context);

        mContext = context;
        mPositionProvider = this;

        mIsIPSEnabled = mIsRunning = mIsWaitingForDelayedPositioningStart = false;
        mEnableCompassBearing = true;
        mLastBearingDegree = MapsIndoorsSettings.NO_BEARING_AVAILABLE_VALUE;
        restartTimer = null;

        mPositionUpdateListeners = new ArrayList<>();
        mStateChangedListeners = new ArrayList<>();

        initialize( context );
    }

    void initialize( @NonNull Context context ) {
        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

        // Register broadcast receivers
        mGPSStateChangeReceiver = new GPSStateChangeReceiver( mContext );
        mGPSStateChangeReceiver.addOnStateChangedListener( this::reportStateToListeners );
        mContext.registerReceiver( mGPSStateChangeReceiver, new IntentFilter( "android.location.PROVIDERS_CHANGED" ) );

        // Bearing sensors
        if( mEnableCompassBearing ) {
            mCompassBearingProvider = new CompassBearingProvider( context );
            mCompassBearingProvider.setBearingChangedListener( onBearingChangedListener );
        }
    }

    void reportStateToListeners( boolean state ) {
        if( mStateChangedListeners != null ) {
            for( int i = mStateChangedListeners.size(); --i >= 0;) {
                if( PSUtils.arePermissionsGranted( mContext, getRequiredPermissions() ) ) {
                    mStateChangedListeners.get( i ).onStateChanged( state );
                } else {
                    mStateChangedListeners.get( i ).onStateChanged( false );
                }
            }
        }
    }

    void requestLocationStart() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval( LOCATION_REQUEST_INTERVAL_IN_MS );
        mLocationRequest.setFastestInterval( LOCATION_REQUEST_FASTEST_INTERVAL_IN_MS );
        mLocationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );

        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().
                addLocationRequest( mLocationRequest );


        LocationServices.getSettingsClient(mContext).checkLocationSettings(builder.build()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                highAccuracyGranted();
            }else {
                requestLocationStartLow();
            }
        });

    }

    void requestLocationStartLow() {
        // to avoid app crash when the gps access is not granted
        try {
            // "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_REQUEST_MIN_TIME_IN_MS,
                    LOCATION_UPDATE_REQUEST_MIN_DISTANCE_IN_METERS,
                    locationLocationListener
            );

            onLocationChanged( mLocationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER ) );

        } catch( SecurityException ex ) {
            // This exception fire up when the gps access is not granted
            stopPositioning( "" );

            if( dbglog.isDeveloperMode() ) {
                dbglog.Log( TAG, "GPS Permission denied : " + ex.getMessage() );
            }
        }
    }

    //region Implements PositionProvider
    @NonNull
    @Override
    public String[] getRequiredPermissions()
    {
        return REQUIRED_PERMISSIONS;
    }

    @Override
    public boolean isPSEnabled()
    {
        return mIsIPSEnabled;
    }

    @Override
    public void startPositioning( @Nullable String arg )
    {
        if( !mIsRunning ) {

            mIsRunning = true;
            mIsWaitingForDelayedPositioningStart = false;

            if( mGoogleApiClient == null ) {
                mGoogleApiClient = new GoogleApiClient.Builder( mContext ).
                        addConnectionCallbacks( googleAPIClientConnectionCallbacks ).
                        addApi( com.google.android.gms.location.LocationServices.API ).
                        build();
            }

            mGoogleApiClient.connect();

            // Setting the sensors for bearing detection
            if( mEnableCompassBearing ) {
                mCompassBearingProvider.start();
            }

            if( mPositionUpdateListeners != null ) {
                if( dbglog.isDeveloperMode() ) {
                    dbglog.Log( TAG, "startPositioning( " + arg + " )" );
                }

                for( final OnPositionUpdateListener listener : mPositionUpdateListeners ) {
                    if( listener != null ) {
                        listener.onPositioningStarted( this );
                    }
                }
            }
        }
    }

    @Override
    public void stopPositioning( @Nullable String arg )
    {
        if( dbglog.isDeveloperMode() ) {
            dbglog.Log( TAG, "stopPositioning( " + arg + " )" );
        }

        if( mIsRunning ) {
            if( mGoogleApiClient != null ) {
                mGoogleApiClient.disconnect();
            }

            if( mCompassBearingProvider != null ) {
                mCompassBearingProvider.stop();
            }

            mIsRunning = mIsWaitingForDelayedPositioningStart = false;
        }
    }

    @Override
    public boolean isRunning()
    {
        return mIsRunning;
    }


    @Override
    public void setProviderId( @Nullable String id )
    {
        mProviderId = id;
    }

    @Override
    public void checkPermissionsAndPSEnabled( @Nullable PermissionsAndPSListener permissionsAndPSListener )
    {
        PSUtils.checkLocationPermissionAndServicesEnabled( getRequiredPermissions(), mContext, permissionsAndPSListener );
    }

    @Override
    @Nullable
    public PositionResult getLatestPosition()
    {
        return mLatestPosition;
    }

    /**
     * Check whether or not a position provider instance is capable of
     * providing positioning data, under current conditions.
     *
     * @param onComplete
     * @return true or false,
     */
    @Override
    protected void checkIfCanDeliver(ReadyListener onComplete) {
        if (onComplete != null) {
            onComplete.onResult();
        }
    }

    @Override
    protected boolean getCanDeliver() {
        return true;
    }

    /**
     * Returns the name of the {@link PositionProvider}
     *
     * @return Name of the PositionProvider
     */
    @Override
    public String getName() {
        return "Google Fused Location";
    }

    /**
     * Returns the versioning of the position provider
     *
     * @return version name string
     */
    @Override
    public String getVersion() {
        return Integer.toString(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE);
    }

    @Override
    public String getAdditionalMetaData() {
        return null;
    }

    @Nullable
    @Override
    public String getProviderId()
    {
        return mProviderId;
    }

    @Override
    public void startPositioningAfter( @IntRange(from = 0, to = Integer.MAX_VALUE) int delayInMs, @Nullable final String arg )
    {
        if( !mIsWaitingForDelayedPositioningStart ) {
            mIsWaitingForDelayedPositioningStart = true;

            final Timer timer = getRestartTimer();
            timer.schedule( new TimerTask()
            {
                @Override
                public void run()
                {
                    startPositioning( arg );
                }
            }, delayInMs );

            timer.cancel();
        }
    }

    @NonNull
    Timer getRestartTimer()
    {
        if( restartTimer == null ) {
            restartTimer = new Timer();
        } else {
            restartTimer.cancel();
        }

        return restartTimer;
    }

    @Override
    public void terminate() {
        stopPositioning( null );

        mPositionUpdateListeners = null;
        mStateChangedListeners = null;

        mLatestPosition = null;
        mContext = null;
    }

    //region IMPLEMENTS BearingChangeListener
    final OnBearingChangedListener onBearingChangedListener = new OnBearingChangedListener() {
        @Override
        public void onBearingChanged( final float bearingInDegree ) {
            if( mLatestPosition == null ) {
                return;
            }

            if( mLatestPosition.getAndroidLocation() == null ) {
                return;
            }

            if( Math.abs( mLastBearingDegree - bearingInDegree ) < BEARING_REPORT_AS_CHANGED_THRESHOLD ) {
                return;
            }

            mLastBearingDegree = bearingInDegree;

            mLatestPosition.setBearing( bearingInDegree );
            mLatestPosition.getAndroidLocation().setBearing( bearingInDegree );

            reportPositionUpdate();
        }
    };
    //endregion

    //region android.location.LocationListener
    final android.location.LocationListener locationLocationListener = new android.location.LocationListener()
    {
        @Override
        public void onLocationChanged( @Nullable final Location location )
        {
            GoogleAPIPositionProvider.this.onLocationChanged( location );
        }

        @Override
        public void onProviderEnabled( @Nullable final String provider )
        {
        }

        @Override
        public void onProviderDisabled( @Nullable final String provider )
        {
        }

        /**
         *
         * @param provider
         * @param status
         * @param extras
         * @deprecated This method was deprecated in API level 29. This callback will never be invoked
         */
        @Override
        public void onStatusChanged( String provider, int status, Bundle extras )
        {
        }
    };
    //endregion

    //region EVENT HANDLERS
    final GoogleApiClient.ConnectionCallbacks googleAPIClientConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected( @Nullable Bundle bundle ) {
            requestLocationStart();
        }

        @Override
        public void onConnectionSuspended( final int cause ) {
            if( cause == CAUSE_SERVICE_DISCONNECTED ) {
                if( dbglog.isDeveloperMode() ) {
                    dbglog.LogW( TAG, "Disconnected. Please re-connect." );
                }
            } else if( cause == CAUSE_NETWORK_LOST ) {
                if( dbglog.isDeveloperMode() ) {
                    dbglog.LogW( TAG, "Network lost. Please re-connect." );
                }
            }
        }
    };

    final GoogleApiClient.OnConnectionFailedListener googleAPIClientOnConnectionFailedListener = connectionResult -> {
        if( dbglog.isDeveloperMode() ) {
            dbglog.LogE( TAG, "Failed to connect to LocationServices. Error (1) code: " + connectionResult.getErrorMessage() + " / " + connectionResult.toString() );
        }
    };

    private void highAccuracyGranted () {
        if( (mGoogleApiClient == null) || MapsIndoorsActivity.isActivityFinishing( mContext ) ) {
            return;
        }

        try {
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
            fusedLocationProviderClient.requestLocationUpdates( mLocationRequest, locationCallback, null);

        } catch( SecurityException ex ) {

            stopPositioning( null );

            if( dbglog.isDeveloperMode() ) {
                dbglog.Log( TAG, "GPS Permission denied : " + ex.getMessage() );
            }
        }
    }


    final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult.getLastLocation() == null) {
                mLatestPosition = null;
                mIPSEnabled = false;
            } else {
                onLocationChanged(locationResult.getLastLocation());
            }

        }
    };

    /**
     *
     * @param location
     */
    final void onLocationChanged( @Nullable final Location location )
    {
        if( location == null ) {
            mLatestPosition = null;
            mIsIPSEnabled = false;
            return;
        }

        if( mIsRunning ) {
            mIsIPSEnabled = true;

            final android.location.Location recLocation = new Location( location );

            final MPPositionResult newLocation = new MPPositionResult( new Point( recLocation ), recLocation.getAccuracy() );

            if( mLastBearingDegree < (MapsIndoorsSettings.NO_BEARING_AVAILABLE_VALUE - BEARING_REPORT_AS_CHANGED_THRESHOLD) ) {
                // ???
                recLocation.setBearing( mLastBearingDegree );
                newLocation.setBearing( mLastBearingDegree );
            }

            newLocation.setAndroidLocation( recLocation );

            // From Google's Santa tracker:
            // "Update our current location only if we've moved at least a metre, to avoid
            // jitter due to lack of accuracy in FusedLocationApi"
            if( mLatestPosition != null ) {

                final Point prevPoint = mLatestPosition.getPoint();
                final Point newPoint = newLocation.getPoint();

                if( (prevPoint != null) && (newPoint != null) )
                {
                    // Check the distance between the prev and new position in 2D (lat/lng)
                    final double dist = prevPoint.distanceTo( newPoint );
                    if( dist <= UPDATE_DISTANCE_THRESHOLD ) {

                        // Get the altitude too. Just imagine the lady/guy is using a lift/elevator/"spiral staircase"...
                        // Use the prev position "android location object" altitude value to run the check
                        final android.location.Location prevLocation = mLatestPosition.getAndroidLocation();
                        if( prevLocation != null ) {
                            final double altDiff = Math.abs( recLocation.getAltitude() - prevLocation.getAltitude() );
                            if( altDiff <= UPDATE_ALTITUDE_THRESHOLD ) {
                                return;
                            }
                        }
                    }
                }
            }

            // GPS always gives the ground level
            newLocation.setFloor( Floor.DEFAULT_GROUND_FLOOR_INDEX );

            mLatestPosition = newLocation;
            mLatestPosition.setProvider( mPositionProvider );
            mLatestPosition.setAndroidLocation( recLocation );

            //setLatestPosition(mLatestPosition);
            reportPositionUpdate();
        }
    }
    //endregion
}
