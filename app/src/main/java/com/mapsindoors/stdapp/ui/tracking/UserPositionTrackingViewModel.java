package com.mapsindoors.stdapp.ui.tracking;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.Route;
import com.mapsindoors.mapssdk.RouteSegmentPath;
import com.mapsindoors.stdapp.ui.common.listeners.RouteTrackingAdapter;
import com.mapsindoors.stdapp.ui.common.listeners.UserPositionTrackingAdapter;

public class UserPositionTrackingViewModel {


    public static final int STATE_NO_LOCATION = 0;
    public static final int STATE_LOCATION_TRACKING_ENABLED = 1;
    public static final int STATE_LOCATION_TRACKING_DISABLED = 2;
    public static final int STATE_COMPASS_TRACKING_ENABLED = 3;
    public static final int STATE_COMPASS_TRACKING_DISABLED = 4;
    private int mCurrentFloor = Integer.MAX_VALUE;

    private Route mCurrentRoute;
    private RouteSegmentPath mCurrentRouteSegmentPath = new RouteSegmentPath(-1, -1);

    private int mCurrentState = STATE_NO_LOCATION;


    private PositionResult mCurrentPos;
    private MapControl mMapControl;
    private GoogleMap mGoogleMap;

    private Context mContext;

    private UserPositionTrackingAdapter mUserPositionTrackingAdapter;
    private RouteTrackingAdapter mRouteTrackingAdapter;

    private boolean mTrackingIdle = false;


    private int moveStarted = -1;

    public UserPositionTrackingViewModel(MapControl mapControl, GoogleMap googleMap, Context context, UserPositionTrackingAdapter userPositionTrackingAdapter) {
        mMapControl = mapControl;
        mGoogleMap = googleMap;
        mContext = context;
        mUserPositionTrackingAdapter = userPositionTrackingAdapter;
        setupOnMapIDleListener();


        mMapControl.addOnCameraMoveStartedListener(reason -> moveStarted = reason);

    }

    public void onFollowMeBtnClick() {

        if (mCurrentPos == null) {

            setState(STATE_NO_LOCATION);

            return;
        }
        switch (mCurrentState) {
            case STATE_LOCATION_TRACKING_DISABLED:
                setState(STATE_LOCATION_TRACKING_ENABLED);
                break;
            case STATE_COMPASS_TRACKING_DISABLED:
                setState(STATE_COMPASS_TRACKING_ENABLED);
                break;
            case STATE_LOCATION_TRACKING_ENABLED:
                setState(STATE_COMPASS_TRACKING_ENABLED);
                break;
            case STATE_COMPASS_TRACKING_ENABLED:
                setState(STATE_LOCATION_TRACKING_ENABLED);
                break;

        }

        onPositionUpdate(mCurrentPos);

    }


    private void setupOnMapIDleListener() {

        mMapControl.addOnCameraIdleListener(() -> {

            if (moveStarted == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {


                if (mCurrentPos == null) {
                    return;
                }
                CameraPosition pos = mGoogleMap.getCameraPosition();

                Point mapCenter = mGoogleMap.getProjection().toScreenLocation(pos.target);


                Point currentUserPosition = mGoogleMap.getProjection().toScreenLocation(mCurrentPos.getPoint().getLatLng());

                float xDiff = mapCenter.x - currentUserPosition.x;
                float yDiff = mapCenter.y - currentUserPosition.y;

                float distance = (float) Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
                // the pixel distance is proportional to the pixel density of the screen so better to use the dp distances to measure
                float distanceInDP = pxToDp(mContext, distance);


                if (distanceInDP > 33) {
                    stopTracking();

                } else {
                    onPositionUpdate(mCurrentPos);
                }


            } else {

                if (!mTrackingIdle) {


                    stopTracking();


                } else {

                    mTrackingIdle = false;
                }
            }


            moveStarted = -1;
        });
    }


    public void stopTracking() {

        switch (mCurrentState) {

            case STATE_LOCATION_TRACKING_ENABLED:
                setState(STATE_LOCATION_TRACKING_DISABLED);
                break;

            case STATE_COMPASS_TRACKING_ENABLED:
                setState(STATE_LOCATION_TRACKING_DISABLED);
                break;
        }

    }

    public void setState(int state) {
        if (mCurrentState == state) {
            return;
        }
        mCurrentState = state;
        mUserPositionTrackingAdapter.trackingStateChanged(mCurrentState);
    }

    private static final float TRACKINGMODE_NO_HEADING_ZOOM_MIN = 15;
    private static final float TRACKINGMODE_NO_HEADING_ZOOM_MAX = 21;
    private static final int TRACKINGMODE_HEADING_FIXED_ZOOM = 21;
    private static final float TRACKINGMODE_HEADING_FIXED_TILT = 45;

    static final float TRACKING_CAMERA_TILT_DEFAULT = 0;

    static final float TRACKING_CAMERA_HEADING_DEFAULT = 0;


    public void onPositionUpdate(PositionResult newPosition) {


        if (newPosition == null) {
            return;
        }

        mCurrentPos = newPosition;
        // once the first location of the user is returned the state no location should be changed
        if (mCurrentState == STATE_NO_LOCATION) {
            setState(STATE_LOCATION_TRACKING_DISABLED);
        }

        if (mCurrentState != STATE_LOCATION_TRACKING_ENABLED && mCurrentState != STATE_COMPASS_TRACKING_ENABLED) {
            return;
        }


        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            final CameraPosition cp = mGoogleMap.getCameraPosition();

            CameraPosition.Builder newCamPos = new CameraPosition.Builder().target(newPosition.getPoint().getLatLng());

            if (mCurrentState == STATE_LOCATION_TRACKING_ENABLED) {

                // TRACKING MODE, NO HEADING
                // Only condition here, check that the current zoom is within the req range
                float cZoom = cp.zoom;

                boolean cZoomIsBelowMin = cZoom < TRACKINGMODE_NO_HEADING_ZOOM_MIN;
                boolean cZoomIsAboveMax = cZoom > TRACKINGMODE_NO_HEADING_ZOOM_MAX;

                if (cZoomIsBelowMin || cZoomIsAboveMax) {
                    newCamPos.zoom(
                            cZoomIsBelowMin
                                    ? TRACKINGMODE_NO_HEADING_ZOOM_MIN
                                    : TRACKINGMODE_NO_HEADING_ZOOM_MAX);
                } else {
                    newCamPos.zoom(cp.zoom);
                }

                newCamPos.bearing(TRACKING_CAMERA_HEADING_DEFAULT).tilt(TRACKING_CAMERA_TILT_DEFAULT);


            } else if (mCurrentState == STATE_COMPASS_TRACKING_ENABLED) {
                newCamPos.
                        zoom(TRACKINGMODE_HEADING_FIXED_ZOOM).
                        tilt(TRACKINGMODE_HEADING_FIXED_TILT);

                if (newPosition.hasBearing()) {
                    newCamPos.bearing(newPosition.getBearing());
                } else {
                    newCamPos.bearing(cp.bearing);
                }

            }


            mTrackingIdle = true;

            if (mCurrentRoute != null) {
                routeTrackingPos(newPosition);
            } else {
                // we handle floor tracking only when there is no route tracking, because it would do the floor tracking automatically with selecting the right leg
                if (newPosition.hasFloor()) {
                    mCurrentFloor = newPosition.getFloor();
                    mMapControl.enableFloorSelector(true);
                    mMapControl.selectFloor(mCurrentFloor);
                }
            }

            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos.build()), 500, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                }

                @Override
                public void onCancel() {
                }
            });


        });


    }

    public void setRoute(Route route) {
        mCurrentRoute = route;
    }


    public void setRouteTrackingAdapter(RouteTrackingAdapter mRouteTrackingAdapter) {
        this.mRouteTrackingAdapter = mRouteTrackingAdapter;
    }

    void routeTrackingPos(PositionResult newPosition) {
        if (mCurrentRoute != null && mRouteTrackingAdapter != null) {

            RouteSegmentPath routeSegement = mCurrentRoute.findNearestSegmentPathFromPoint(newPosition.getPoint(), newPosition.getFloor());

            if (routeSegement.leg != mCurrentRouteSegmentPath.leg || routeSegement.step != mCurrentRouteSegmentPath.step) {


                if (routeSegement.leg != mCurrentRouteSegmentPath.leg) {
                    mTrackingIdle = true;
                    mRouteTrackingAdapter.selectRouteLeg(mCurrentRoute, routeSegement.leg);
                }


                mRouteTrackingAdapter.routeSegmentPathChanged(mCurrentRoute, routeSegement.leg, routeSegement.step);

                mCurrentRouteSegmentPath = routeSegement;
            }

        }
    }
    float pxToDp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }


}
