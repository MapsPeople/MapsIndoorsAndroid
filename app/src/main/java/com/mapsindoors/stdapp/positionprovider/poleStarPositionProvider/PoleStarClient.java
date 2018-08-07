package com.mapsindoors.stdapp.positionprovider.poleStarPositionProvider;

public class PoleStarClient // implements NAOLocationListener, NAOSensorsListener
{
//
//    public static final String TAG = PoleStarClient.class.getSimpleName();
//
//    NAOLocationHandle mHandle;
//    Context mContext;
//    LocationChangeListener mLocationChangeListener;
//
//    public PoleStarClient(Context context){
//
//        mContext = context;
//
//        String poleStarAPIKey =  mContext.getResources().getString(R.string.polestar_api_key);
//
//        mHandle = new NAOLocationHandle(mContext, NAOService.class, poleStarAPIKey, this, this);
//
//    }
//
//
//    public void start() {
//        if (mHandle != null) {
//            mHandle.start();
//        }
//    }
//
//    public void stop() {
//        if (mHandle != null) {
//            mHandle.stop();
//
//        }
//    }
//
//    public LocationChangeListener getLocationChangeListener() {
//        return mLocationChangeListener;
//    }
//
//    public void setLocationChangeListener(LocationChangeListener mLocationChangeListener) {
//        this.mLocationChangeListener = mLocationChangeListener;
//    }
//
//
//    @Override
//    public void onLocationChanged(Location location) {
//        Log.d(TAG, "onLocationChanged : long: " + location.getLongitude() + "/lat: " + location.getLatitude() ) ;
//
//        if(mLocationChangeListener != null){
//            mLocationChangeListener.onLocationChanged(location);
//        }
//    }
//
//    @Override
//    public void onLocationStatusChanged(TNAOFIXSTATUS tnaofixstatus) {
//        Log.d(TAG, "onLocationStatusChanged : " + tnaofixstatus ) ;
//
//    }
//
//    @Override
//    public void onEnterSite(String s) {
//        Log.d(TAG, "onEnterSite : " + s) ;
//
//    }
//
//    @Override
//    public void onExitSite(String s) {
//        Log.d(TAG, "onLocationChanged : " ) ;
//
//    }
//
//    @Override
//    public void onError(NAOERRORCODE naoerrorcode, String s) {
//        Log.d(TAG, "onExitSite : " + s) ;
//
//    }
//
//    @Override
//    public void requiresCompassCalibration() {
//        Log.d(TAG, "requiresCompassCalibration  " ) ;
//
//    }
//
//    @Override
//    public void requiresWifiOn() {
//        Log.d(TAG, "requiresWifiOn  " ) ;
//
//    }
//
//    @Override
//    public void requiresBLEOn() {
//        Log.d(TAG, "requiresBLEOn " ) ;
//
//    }
//
//    @Override
//    public void requiresLocationOn() {
//        Log.d(TAG, "requiresLocationOn " ) ;
//
//    }
}
