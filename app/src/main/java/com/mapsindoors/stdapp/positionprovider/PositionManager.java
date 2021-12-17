package com.mapsindoors.stdapp.positionprovider;

import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;

import com.mapsindoors.mapssdk.LocationDisplayRule;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnPositionUpdateListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.debug.DebugVisualizer;
import com.mapsindoors.stdapp.ui.tracking.UserPositionTrackingViewModel;

/**
 * The Position Manager is responsible for handling all things positioning related, in the app.
 * It manages the position provider, and contains listeners for the app to work with position providers.
 */
public class PositionManager {

    private final MapControl mMapControl;
    private final MapsIndoorsActivity mActivity;
    private final PositionProviderManager mPositionProviderManager;
    private AppPositionProvider mPositionProvider;

    private boolean mLastBlueDotBearingState;
    private LocationDisplayRule mBlueDotDisplayRule;
    private LocationDisplayRule mBlueDotWithArrowDisplayRule;
    private LocationDisplayRule mBlueDotWithoutArrowDisplayRule;

    private final DebugVisualizer mPositionProviderDebugWindow;

    private final OnPositionProviderChangedListener interceptionListener;
    private final Handler mHandler;
    private boolean shouldReconsider = true;

    private static final int RECONSIDER_PROVIDER_TIME_THRESHOLD_MS = 1000 * 5;

    /**
     * Constructor
     * @param context
     * @param mapControl
     */
    public PositionManager(MapsIndoorsActivity context, MapControl mapControl, OnPositionProviderChangedListener onPositionProviderChangedListener){
        mActivity = context;
        mPositionProviderDebugWindow = mActivity.getPositionProviderDebugVisualizer();
        mMapControl = mapControl;
        mMapControl.showUserPosition(true);


        interceptionListener = positionProvider -> {
            mActivity.runOnUiThread(() -> setupPositionProvider(positionProvider));
            onPositionProviderChangedListener.onPositionProviderChanged(positionProvider);
        };

        mPositionProviderManager = new PositionProviderManager(context, interceptionListener);

        mHandler = new Handler();
        mHandler.postDelayed(periodicReconsideration, RECONSIDER_PROVIDER_TIME_THRESHOLD_MS);
    }

    public void onPause(){
        mPositionProviderManager.onPause();
    }

    public void onResume(){
        mPositionProviderManager.onResume();
    }

    /**
     * Sets up a position provider, picking the best of the bunch, from the PositionProviderManager.
     * Upon switching provider, listeners and MapsIndoors are configured.
     * The old provider is stopped, and the new is started.
     */
    private void setupPositionProvider(AppPositionProvider newPositionProvider){
        if (newPositionProvider != null && newPositionProvider != mPositionProvider) {
            if(mPositionProvider != null){
                mPositionProvider.removeOnPositionUpdateListener(onPositionUpdateListener);
                mPositionProvider.removeOnStateChangedListener(this::onPositionProviderStateChanged);
                MapsIndoors.stopPositioning();
            }

            mPositionProvider = newPositionProvider;
            MapsIndoors.setPositionProvider(mPositionProvider);
            mPositionProvider.addOnPositionUpdateListener( onPositionUpdateListener );
            mPositionProvider.addOnStateChangedListener(this::onPositionProviderStateChanged);
            MapsIndoors.startPositioning();
            mPositionProvider.reportPositionUpdate();
        }
    }


    /**
     * Callback, updating according to the position provider state.
     * Used to facilitate a downgrade to a lesser provider, when the current gives up.
     * @param isEnabled
     */
    private void onPositionProviderStateChanged(boolean isEnabled) {
        if (mMapControl != null) {
            if (isEnabled) {
                mMapControl.showUserPosition(true);
            } else {
                mMapControl.showUserPosition(false);

                // Reconsider the choice of position provider, as the current provider doesn't seem up to the task
                interceptionListener.onPositionProviderChanged(mPositionProviderManager.getProvider());
            }
            MapsIndoors.startPositioning();
        }
        mActivity.getUserPositionTrackingViewModel().setState(isEnabled ? UserPositionTrackingViewModel.STATE_LOCATION_TRACKING_DISABLED : UserPositionTrackingViewModel.STATE_NO_LOCATION);
    }

    /**
     * Given a PositionResult object {@link PositionResult} build strings, and apply to appropriate debug fields
     * @param positionResult
     */
    private void updateDebugWindow(@NonNull PositionResult positionResult){
        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append("Lat: \t");
        strBuilder.append(positionResult.getPoint().getLat());
        strBuilder.append("\nLng: \t");
        strBuilder.append(positionResult.getPoint().getLng());
        strBuilder.append("\n");

        strBuilder.append("Floor: \t");
        strBuilder.append(positionResult.getFloor());
        strBuilder.append("\n");

        strBuilder.append("Bearing: \t");
        strBuilder.append(positionResult.getBearing());
        strBuilder.append("\n");

        strBuilder.append("Accuracy: \t");
        strBuilder.append(positionResult.getAccuracy());
        strBuilder.append("\n");

        mPositionProviderDebugWindow.updateDebugField("provider", mPositionProvider.getName());

        mPositionProviderDebugWindow.updateDebugField("position", strBuilder.toString());

        if (mPositionProvider.getAdditionalMetaData() == null || mPositionProvider.getAdditionalMetaData().isEmpty()) {
            mPositionProviderDebugWindow.setShowDebugField("meta", false);
        } else {
            String metaString = mPositionProvider.getAdditionalMetaData();
            mPositionProviderDebugWindow.updateDebugField("meta", metaString);
            mPositionProviderDebugWindow.setShowDebugField("meta", true);
        }

    }

    final Runnable periodicReconsideration = new Runnable() {
        public void run() {
            if (shouldReconsider) {
                interceptionListener.onPositionProviderChanged(mPositionProviderManager.getProvider());
                mHandler.postDelayed(periodicReconsideration, RECONSIDER_PROVIDER_TIME_THRESHOLD_MS);
            }
        }
    };

    /**
     * Listener, updating the UI upon a new positioning.
     * Note, on each position update, if enough time has passed or the accuracy is bad,
     * an attempt is made to elect a new (and higher priority) provider.
     */
    private final OnPositionUpdateListener onPositionUpdateListener = new OnPositionUpdateListener() {
        @Override
        public void onPositioningStarted( @NonNull PositionProvider provider ) { }

        @Override
        public void onPositionFailed( @NonNull PositionProvider provider ) { }

        @Override
        public void onPositionUpdate( @NonNull PositionResult pos ) {
            mActivity.getUserPositionTrackingViewModel().onPositionUpdate(pos);

            mActivity.runOnUiThread( () -> {
                updateDebugWindow(pos);
            });

            //Switching between the 2 blue dot modes, with bearing and without
            if (pos.hasBearing() == mLastBlueDotBearingState) {
                return;
            }

            mActivity.runOnUiThread( () -> {
                boolean didBearingStateChange;
                if( pos.hasBearing() ) {
                    if( mBlueDotWithArrowDisplayRule == null ) {
                        mBlueDotWithArrowDisplayRule = new LocationDisplayRule.Builder( "BlueDotRule" ).
                                setVectorDrawableIcon( R.drawable.ic_user_location_marker_icon_arrow, 23, 23 ).
                                setShowLabel( false ).setLabel( null ).
                                build();
                    }
                    mBlueDotDisplayRule = mBlueDotWithArrowDisplayRule;
                } else {
                    if( mBlueDotWithoutArrowDisplayRule == null ) {
                        mBlueDotWithoutArrowDisplayRule = new LocationDisplayRule.Builder( "BlueDotRule" ).
                                setVectorDrawableIcon( R.drawable.ic_user_location_marker_icon_default, 23, 23 ).
                                setShowLabel( false ).setLabel( null ).
                                build();
                    }
                    mBlueDotDisplayRule = mBlueDotWithoutArrowDisplayRule;
                }

                didBearingStateChange = mMapControl.getPositionIndicator().setIconFromDisplayRule(mBlueDotDisplayRule);

                if (didBearingStateChange) {
                    mLastBlueDotBearingState = pos.hasBearing();
                }
            });
        }
    };

    /**
     * Returns the current position provider
     * @return
     */
    public PositionProvider getCurrentProvider(){
        return mPositionProvider;
    }

    /**
     * OnClickListener for the follow-me button
     * @param view
     */
    public void onFollowMeButtonClickListener(View view){
        GoogleAnalyticsManager.reportEvent(mActivity.getString(R.string.fir_event_Map_Blue_Dot_clicked), null);
        final PositionProvider positionProvider = MapsIndoors.getPositionProvider();
        if( positionProvider != null ) {
            positionProvider.checkPermissionsAndPSEnabled( new PermissionsAndPSListener() {
                @Override
                public void onPermissionDenied() {
                    mActivity.setLocationPermissionGranted(false);
                    mActivity.getUserPositionTrackingViewModel().setState(UserPositionTrackingViewModel.STATE_NO_LOCATION);
                }

                @Override
                public void onPermissionGranted() { }

                @Override
                public void onGPSPermissionAndServiceEnabled() {
                    // It could be that the GPS is already running, but the permission was not granted
                    mActivity.setLocationPermissionGranted(true);

                    if (mActivity.getGoogleMap() == null || mMapControl == null) {
                        return;
                    }

                    if (!MapsIndoors.getPositionProvider().isRunning()) {
                        MapsIndoors.startPositioning();
                    }

                    if (!mMapControl.isUserPositionShown()) {
                        mMapControl.showUserPosition(true);
                    }

                    mActivity.getUserPositionTrackingViewModel().onFollowMeBtnClick();
                }

                @Override
                public void onPermissionRequestError() { }
            });
        }
    }

    public void onDestroy() {
        mPositionProvider = null;
        shouldReconsider = false;
        mPositionProviderManager.onDestroy();
    }

}
