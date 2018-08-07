package com.mapsindoors.stdapp.positionprovider.poleStarPositionProvider;

public class PoleStarPositionProvider // implements PositionProvider
{
//
//    /** How much the user needs to move before reporting a position update, in meters */
//    private static final double UPDATE_DISTANCE_THRESHOLD = 1.0;
//    private static final double UPDATE_ALTITUDE_THRESHOLD = 2.0;
//
//    PositionResult mLatestPosition;
//
//    Context mContext;
//    PoleStarClient mPoleStarClient;
//    boolean isRunning = false;
//
//    private List<OnStateChangedListener> mStateChangedlistenersList;
//    private List<OnPositionUpdateListener> mPositionUpdateListeners;
//
//    private static final String[] REQUIRED_PERMISSIONS = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.WRITE_EXTERNAL_STORAGE"};
//
//
//    public PoleStarPositionProvider(Context context ){
//        mContext = context;
//
//        mStateChangedlistenersList = new ArrayList<>();
//
//        mPoleStarClient = new PoleStarClient(mContext);
//
//        mPoleStarClient.setLocationChangeListener(new LocationChangeListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                updatePosition(location);
//            }
//        });
//
//    }
//
//
//    void updatePosition(Location location){
//        if (location == null) {
//            mLatestPosition = null;
//            return;
//        }
//
//        if( isRunning() ) {
//
//            MPPositionResult newLocation = new MPPositionResult(
//                    new Point( location ),
//                    location.getAccuracy(),
//                    location.getBearing()
//            );
//
//            // From Google's Santa tracker:
//            // "Update our current location only if we've moved at least a metre, to avoid
//            // jitter due to lack of accuracy in FusedLocationApi"
//            if( mLatestPosition != null ) {
//
//                // Check the distance between the prev and new position in 2D (lat/lng)
//                final double dist = mLatestPosition.getPoint().distanceTo( newLocation.getPoint() );
//                if( dist <= UPDATE_DISTANCE_THRESHOLD ) {
//
//                    // Get the altitude too. Just imagine the lady/guy is using a lift/elevator/"spiral staircase"...
//                    // Use the prev position "android location object" altitude value to run the check
//                    Location prevLocation = mLatestPosition.getAndroidLocation();
//                    if( prevLocation != null )
//                    {
//                        final double altDiff = Math.abs( location.getAltitude() - prevLocation.getAltitude() );
//                        if( altDiff <= UPDATE_ALTITUDE_THRESHOLD )
//                        {
//                            return;
//                        }
//                    }
//                }
//            }
//
//            mLatestPosition = newLocation;
//            mLatestPosition.setProvider( this );
//            mLatestPosition.setAndroidLocation( location );
//
//            if( mPositionUpdateListeners != null ) {
//                for( OnPositionUpdateListener listener : mPositionUpdateListeners ) {
//                    if( listener != null ) {
//                        listener.onPositionUpdate( mLatestPosition );
//                    }
//                }
//            }
//
//    }
//
//    }
//    @NonNull
//    @Override
//    public String[] getRequiredPermissions() {
//        return  REQUIRED_PERMISSIONS;
//    }
//
//    @Override
//    public boolean isPSEnabled() {
//        return false;
//    }
//
//    @Override
//    public void startPositioning(@Nullable String s) {
//
//        if (! isRunning ) {
//
//            if(mPoleStarClient != null){
//
//                mPoleStarClient.start();
//                isRunning = true;
//
//            }
//        }
//
//    }
//
//    @Override
//    public void stopPositioning(@Nullable String s) {
//
//        if ( isRunning ) {
//            if(mPoleStarClient != null){
//
//                mPoleStarClient.stop();
//                isRunning = false;
//
//            }
//        }
//
//
//    }
//
//    @Override
//    public boolean isRunning() {
//        return isRunning;
//    }
//
//    @Override
//    public void addOnPositionUpdateListener(@Nullable OnPositionUpdateListener onPositionUpdateListener) {
//        if( onPositionUpdateListener != null ) {
//            if( mPositionUpdateListeners == null ) {
//                mPositionUpdateListeners = new ArrayList<>();
//            }
//
//            mPositionUpdateListeners.remove( onPositionUpdateListener );
//            mPositionUpdateListeners.add( onPositionUpdateListener );
//        }
//
//    }
//
//    @Override
//    public void removeOnPositionUpdateListener(@Nullable OnPositionUpdateListener onPositionUpdateListener) {
//        if( onPositionUpdateListener != null ) {
//            if( mPositionUpdateListeners != null ) {
//                mPositionUpdateListeners.remove( onPositionUpdateListener );
//                if( mPositionUpdateListeners.isEmpty() ) {
//                    mPositionUpdateListeners = null;
//                }
//            }
//        }
//    }
//
//    @Override
//    public void setProviderId(@Nullable String s) {
//
//    }
//
//    @Override
//    public void addOnstateChangedListener(OnStateChangedListener onStateChangedListener) {
//        mStateChangedlistenersList.add(onStateChangedListener);
//
//    }
//
//    @Override
//    public void removeOnstateChangedListener(OnStateChangedListener onStateChangedListener) {
//        mStateChangedlistenersList.remove(onStateChangedListener);
//
//    }
//
//    @Override
//    public void checkPermissionsAndPSEnabled(PermissionsAndPSListener permissionsAndPSListener) {
//        PSUtils.checkLocationPermissionAndServicesEnabled(getRequiredPermissions(),mContext,permissionsAndPSListener);
//
//    }
//
//    @Nullable
//    @Override
//    public String getProviderId() {
//        return null;
//    }
//
//    @Nullable
//    @Override
//    public PositionResult getLatestPosition() {
//        return mLatestPosition;
//    }
//
//    @Override
//    public void startPositioningAfter(int delay, @Nullable String s) {
//
//        Timer restartTimer = new Timer();
//
//        restartTimer.schedule( new TimerTask() {
//            @Override
//            public void run()
//            {
//                startPositioning( s );
//            }
//        }, delay );
//
//    }
//
//    @Override
//    public void terminate() {
//
//    }
//
//
}
