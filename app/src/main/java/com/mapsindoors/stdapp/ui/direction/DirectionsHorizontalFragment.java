package com.mapsindoors.stdapp.ui.direction;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import com.mapsindoors.mapssdk.MPDirectionsRenderer;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.OnLegSelectedListener;
import com.mapsindoors.mapssdk.Route;
import com.mapsindoors.mapssdk.RouteCoordinate;
import com.mapsindoors.mapssdk.RouteLeg;
import com.mapsindoors.mapssdk.RouteStep;
import com.mapsindoors.mapssdk.SphericalUtil;
import com.mapsindoors.mapssdk.dbglog;

import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.listeners.IDirectionPanelListener;
import com.mapsindoors.stdapp.models.UIRouteNavigation;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.direction.listeners.RouteListener;
import com.mapsindoors.stdapp.ui.direction.models.RoutingEndPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Fragment that shows routes on the map (using mDirectionsRenderer)
 */
public class DirectionsHorizontalFragment extends DirectionsFragment
        implements
        IDirectionPanelListener,
        OnLegSelectedListener {
    static final String TAG = DirectionsHorizontalFragment.class.getSimpleName();

    private GoogleMap mGoogleMap;
    private int mCurrentRouteNaviIndex;

    private RouteListener mRouteListener;

    private List<RouteCoordinate> mTmpRCPointList;

    private HorizontalScrollView mScrollingContainerLayout;
    // Rendering object used to draw routes on top of the google map.
    private MPDirectionsRenderer mMyDirectionsRenderer;
    private View mPanelToolbar, mPanelControls;
    private LinearLayout mBackgroundLayout, mForegroundLayout;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private ImageButton mCancelBtn;

    /**
     * Viewport screen coordinates TL, TR, BL, BR
     */
    private int mMapViewPortScreenWidth, mMapViewPortScreenHeight, mViewPortPadding;
    private android.graphics.Point mViewPortScreenCenter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.fragment_directions_horizontal, container);
        }

        setActive(false);

        return mMainView;
    }

    @Override
    public boolean onBackPressed() {
        if (isActive()) {
            closeAndOpenMenu();
        }
        return true;
    }

    /**
     * Finds the different layouts and interactables in the fragment
     * @param view
     */
    protected void setupView(View view) {
        mPanelToolbar = view.findViewById(R.id.directions_menu_horiz_toolbar);
        mPanelControls = view.findViewById(R.id.directions_fullmenu_horiz_controls);
        mScrollingContainerLayout = view.findViewById(R.id.directions_menu_horiz_horizontalScrollView);
        mBackgroundLayout = view.findViewById(R.id.directions_background);
        mForegroundLayout = view.findViewById(R.id.directions_foreground);
        mNextButton = view.findViewById(R.id.directions_menu_horiz_button_next);
        mPreviousButton = view.findViewById(R.id.directions_menu_horiz_button_prev);
        mCancelBtn = view.findViewById(R.id.directions_menu_horiz_toolbar_button_back);
    }

    /**
     *
     * @param activity
     * @param map
     */
    public void init(@NonNull final MapsIndoorsActivity activity, @NonNull GoogleMap map) {
        mActivity = activity;
        mContext = activity;
        mMapControl = activity.getMapControl();

        mGoogleMap = map;

        mViewPortScreenCenter = new android.graphics.Point(0, 0);

        MapsIndoorsHelper.init(activity);

        mMyDirectionsRenderer = new MPDirectionsRenderer(mContext, map, mMapControl, this);
        mMyDirectionsRenderer.setCameraAnimated(false);

        mMyDirectionsRenderer.setPrimaryColor(ContextCompat.getColor(mContext, R.color.directionLegColor));
        mMyDirectionsRenderer.setAccentColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        mMyDirectionsRenderer.setTextColor(ContextCompat.getColor(mContext, R.color.white));

        mTmpRCPointList = new ArrayList<>();
        mIsLegIndoors = true;
        mSelectedTravelMode = MapsIndoorsHelper.VEHICLE_WALKING;
        mNextButton.setOnClickListener(v -> routeNavigateToNext());
        mPreviousButton.setOnClickListener(v -> routeNavigateToPrev());
        mCancelBtn.setOnClickListener(v -> closeAndOpenMenu());
    }

    /**
     *
     */
    void closeAndOpenMenu() {
        setActive(false);
        ((MapsIndoorsActivity) mContext).openDrawer(true);
    }

    /**
     *
     * @param origin
     * @param destination
     */
    public void setStartEndLocations(@NonNull RoutingEndPoint origin, @NonNull RoutingEndPoint destination) {

        if (mOrigin != null) {
            final MPLocation loc = mOrigin.getLocation();
            if (loc != null) {
                if (loc.isInfoWindowShown()) {
                    loc.hideInfoWindow();
                }
            }
        }

        if (mDestination != null) {
            final MPLocation loc = mDestination.getLocation();
            if (loc != null) {
                if (loc.isInfoWindowShown()) {
                    loc.hideInfoWindow();
                }
            }
        }

        mOrigin = origin;
        mDestination = destination;
    }

    /**
     *
     * @param travelMode
     */
    public void setCurrentTravelMode(@Nullable String travelMode) {
        mSelectedTravelMode = MapsIndoorsHelper.travelModeToVehicle(travelMode);
    }

    /**
     *
     * @param route
     * @param legIndexToSelect
     */
    public void renderRoute(@Nullable final Route route, final int legIndexToSelect) {
        final Activity activity = mActivity;

        if (route == null || activity == null) {
            return;
        }

        if (route != mCurrentRoute) {

            mActivity.runOnUiThread(() -> {

                clearRoute();
                mCurrentRouteNaviIndex = 0;

                mMyDirectionsRenderer.setRoute(route);

                mUINaviList.clear();

                renderDirectionsView(route);
                mCurrentRoute = route;

            });

            mCurrentRoute = route;
        }

        routeNavigateToIndex(legIndexToSelect, false);
    }


    /**
     * Clear the route.
     */
    public void clearRoute() {
        if (mMyDirectionsRenderer != null) {
            mMyDirectionsRenderer.clear();
        }
    }

    /**
     * Hides or shows the direction view
     */
    public void setActive(boolean active) {
        setShow(active);

        if (mActivity != null) {
            if (active) {
                mActivity.horizontalDirectionsPanelWillOpen();
            } else {
                mActivity.horizontalDirectionsPanelWillClose();
            }
        }

        if ((mMapControl != null) && (mMapControl.getFloorSelector() != null)) {
            if (active) {
                mMapControl.enableFloorSelector(false);

                {
                    int panelH = getPanelHeight();
                    View vv = ((MapsIndoorsActivity) mContext).getGMapFragment().getView();

                    int mapH = vv.getHeight();
                    final int mapViewH = mapH - panelH;
                    final int mapViewW = vv.getWidth();

                    mMapViewPortScreenWidth = mapViewW;
                    mMapViewPortScreenHeight = mapViewH;

                    mViewPortScreenCenter.x = mapViewW >> 1;
                    mViewPortScreenCenter.y = mapH - (mapViewH >> 1);
                    mViewPortPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.directions_map_viewport_padding), mContext.getResources().getDisplayMetrics());

                }
            } else {
                // Remove the flag and show the floor selector
                mMapControl.enableFloorSelector(true);
                mMapControl.selectFloor(mMapControl.getCurrentFloorIndex());
            }
        }
    }

    /**
     *
     * @param show
     */
    public void setShow(boolean show) {
        if (isActive() == show) {
            return;
        }

        if (mMainView != null) {
            mMainView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * Sums up the three panel heights (PanelToolbar, ScrollingContainer and PanelControls)
     * @return
     */
    public int getPanelHeight() {
        return mPanelToolbar.getHeight() + mScrollingContainerLayout.getHeight() + mPanelControls.getHeight();
    }

    /**
     * Highlights the given route leg
     * @param legIndex
     * @param itemIndex
     */
    public void onLegSelected(final int legIndex, int itemIndex) {
        onStepLegSelected(legIndex, 0, true);
    }

    /**
     *
     * @param legIndex
     * @param stepIndex
     * @param itemIndex
     */
    public void onStepSelected(final int legIndex, final int stepIndex, int itemIndex) {
        onStepLegSelected(legIndex, stepIndex, false);
    }

    /**
     * Contains the implementation of onLegSelected and onStepSelected
     * @param legIndex
     * @param stepIndex
     * @param isLeg
     */
    private void onStepLegSelected(final int legIndex, final int stepIndex, boolean isLeg) {
        mMyDirectionsRenderer.setAlpha(255);

        new Handler(mContext.getMainLooper()).postDelayed(() -> {

            if (isLeg) {
                mMyDirectionsRenderer.setRouteLegIndex(legIndex);
            } else {
                mMyDirectionsRenderer.setRouteLegIndex(legIndex, stepIndex);
            }

            RouteLeg nextLeg = (legIndex < mCurrentLegs.size() - 1) ? mCurrentLegs.get(legIndex + 1) : null;
            RouteLeg currentLeg = mCurrentLegs.get(legIndex);
            final int legFloor;

            if (nextLeg != null && nextLeg.isMapsIndoors() && !currentLeg.isMapsIndoors()) {
                legFloor = nextLeg.getEndPoint().getZIndex();
            } else {
                legFloor = mMyDirectionsRenderer.getLegFloor();
            }

            if (mMapControl.getCurrentFloorIndex() != legFloor) {
                mMapControl.selectFloor(legFloor);

            }
        }, 100);
    }

    /**
     *
     * @param nextLegIndex
     */
    public void onLegSelected(int nextLegIndex) {
        if (mCurrentRoute != null) {
            int legCount = mCurrentRoute.getLegs().size();

            if (nextLegIndex < legCount) {
                routeNavigateToIndex(legToUINaviIndex(nextLegIndex), true);
            }
        }
    }

    /**
     * Maps a leg index to a navi segment index
     * @param legIndex
     */
    int legToUINaviIndex(@IntRange(from = 0) int legIndex) {
        int res = 0;

        if (mCurrentRoute != null) {
            final int legCount = mCurrentRoute.getLegs().size();

            if (legIndex < legCount) {
                final boolean travelModeIsTransit = mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_TRANSIT;

                final List<RouteLeg> legs = mCurrentRoute.getLegs();

                for (int i = 0; i < legCount; i++) {
                    final RouteLeg cLeg = legs.get(i);
                    if (i == legIndex) {
                        break;
                    }

                    if (travelModeIsTransit) {
                        final List<RouteStep> legSteps = cLeg.getSteps();
                        boolean isCurrentLegIndoors = MapsIndoorsHelper.isStepInsideBuilding(legSteps.get(0));
                        res += !isCurrentLegIndoors ? cLeg.getSteps().size() : 1;
                    } else {
                        res++;
                    }
                }
            }
        }
        return res;
    }


    List<RouteLeg> mCurrentLegs;

    /**
     * Populate the directions view with the legs from a route
     * @param route
     */
    void renderDirectionsView(Route route) {
        mMapControl.setMapPadding(0,0,0,getView().getHeight() );
        mActivity.setPositioningBtnBottomPadding(getView().getHeight());

        resetLegs();
        mCurrentLegs = route.getLegs();
        List<RouteLeg> legs = route.getLegs();

        mIsLegIndoors = MapsIndoorsHelper.isStepInsideBuilding(legs.get(0).getSteps().get(0));

        for (int i = 0, aLen = legs.size(); i < aLen; i++) {
            addLeg(legs.get(i), i);
        }

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View foregroundItem = inflater.inflate(R.layout.control_directions_menu_item_fg, null, true);
        mForegroundLayout.addView(foregroundItem);

        ImageView circleImageView = foregroundItem.findViewById(R.id.dir_horiz_circleImageView);
        TextView titleTextView = foregroundItem.findViewById(R.id.dir_horiz_titleTextView);

        circleImageView.setBackgroundResource(R.drawable.ic_direction_end_marker);

        String locationLabel = mDestination.getLocationName(mActivity);
        String details = mDestination.getFormattedDetails(mActivity);
        if(details != null){
            locationLabel = String.format(Locale.US, "%s\n(%s)", locationLabel, details);
        } else {
            locationLabel = String.format(Locale.US, "%s", locationLabel);
        }

        titleTextView.setText(locationLabel);

        setActive(true);

        mBackgroundLayout.setVisibility(View.VISIBLE);
        mForegroundLayout.setVisibility(View.VISIBLE);

    }

    /**
     * Remove all legs from the view leaving it blank
     */
    private void resetLegs() {
        mBackgroundLayout.removeAllViewsInLayout();
        mForegroundLayout.removeAllViewsInLayout();
    }

    /**
     *
     */
    void routeNavigateToNext() {
        int naviStepsCount = mUINaviList.size();

        if (mCurrentRouteNaviIndex < (naviStepsCount - 1)) {
            routeNavigateToIndex(mCurrentRouteNaviIndex + 1, true);
        }
    }

    /**
     *
     */
    void routeNavigateToPrev() {
        if (mCurrentRouteNaviIndex >= 1) {
            routeNavigateToIndex(mCurrentRouteNaviIndex - 1, true);
        }
    }

    /**
     *
     * @param index
     * @param animate
     */
    void routeItemClickCallback(int index, boolean animate) {
        mActivity.getUserPositionTrackingViewModel().stopTracking();
        routeNavigateToIndex(index, animate);
    }

    /**
     *
     * @param index the index of the route
     * @param animate whether the route should be animated
     */
    @Override
    public void routeNavigateToIndex(int index, boolean animate) {

        super.routeNavigateToIndex(index, animate);

        mCurrentRouteNaviIndex = index;

        UIRouteNavigation currentNavObj = mUINaviList.get(index);

        int itemIndex = currentNavObj.bgViewIndex;

        boolean isOutDoorsStep = !currentNavObj.isIndoors;

        LinearLayout bg = mBackgroundLayout;
        int bgChildCount = bg.getChildCount();
        if ((itemIndex >= 0) && (itemIndex < bgChildCount)) {
            for (int i = 0; i < bgChildCount; i++) {
                bg.getChildAt(i)
                        .setBackgroundColor((i != itemIndex)
                                ? Color.TRANSPARENT
                                : ContextCompat.getColor(mContext, R.color.dir_panel_selected_step));
            }
        }

        mRouteListener.onSelectedLegChanged(index);

        final RouteCoordinate entry_p0 = currentNavObj.p0;
        final RouteCoordinate entry_p1 = currentNavObj.p1;

        if (currentNavObj.latLngBounds != null) {
            final LatLngBounds legBounds = currentNavObj.latLngBounds;

            // In very small bounding boxes, LatLngBounds.getCenter will fail ...
            final LatLng boxCenter = SphericalUtil.interpolate(legBounds.northeast, legBounds.southwest, 0.5f);

            //
            float targetBearing = 0;
            if (currentNavObj.alignWithPathEntryPoints && (entry_p0 != null) && (entry_p1 != null)) {
                targetBearing = currentNavObj.targetBearing;
            }

            // - If map zoom is <= 10: Set tilt (viewing angle) to 30 degrees
            // - If map zoom is > 10: Set tilt (viewing angle) to 45 degrees
            CameraPosition currCamPos = mGoogleMap.getCameraPosition();
            float cZoom = currCamPos.zoom;
            final int targetTilt = (cZoom <= 10) ? MapsIndoorsSettings.MAP_TILT_WHEN_ZOOM_IS_LESS_OR_EQUAL_TO_10
                    : MapsIndoorsSettings.MAP_TILT_WHEN_ZOOM_IS_GREATER_TO_10;

            CameraPosition.Builder dstCamPosBuilder;

            if (currentNavObj.alignWithPathEntryPoints) {
                float targetZoom = mGoogleMap.getMaxZoomLevel();

                if (mUINaviList.size() - 1 == index) {
                    mActivity.getSelectionManager().selectLocation(mDestination.getLocation(), false, false, false, false);
                }

                int tiltIndex = (targetTilt == MapsIndoorsSettings.MAP_TILT_WHEN_ZOOM_IS_LESS_OR_EQUAL_TO_10) ? 0 : 1;

                if (currentNavObj.gmapCameraPositionBuilder[tiltIndex] == null) {


                    dstCamPosBuilder = new CameraPosition.Builder();
                    dstCamPosBuilder
                            .target(boxCenter)
                            .zoom(targetZoom)
                            .tilt(targetTilt)
                            .bearing(targetBearing);

                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(dstCamPosBuilder.build()));

                    Projection prj = mGoogleMap.getProjection();
                    LatLng dstCalcTarget = prj.fromScreenLocation(mViewPortScreenCenter);

                    CameraUpdate currCamPosUpdate = CameraUpdateFactory.newCameraPosition(currCamPos);

                    // Use now the AABB calculated after rotating the path
                    {
                        mGoogleMap.moveCamera(currCamPosUpdate);

                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                                currentNavObj.latLngBounds,
                                mMapViewPortScreenWidth, mMapViewPortScreenHeight, mViewPortPadding));

                        targetZoom = mGoogleMap.getCameraPosition().zoom;
                    }

                    mGoogleMap.moveCamera(currCamPosUpdate);

                    dstCamPosBuilder
                            .zoom(targetZoom)
                            .target(dstCalcTarget);

                    currentNavObj.gmapCameraPositionBuilder[tiltIndex] = dstCamPosBuilder;
                } else {
                    dstCamPosBuilder = currentNavObj.gmapCameraPositionBuilder[tiltIndex];
                }
            } else {
                if (currentNavObj.gmapCameraPositionBuilder[0] == null) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                            legBounds,
                            mMapViewPortScreenWidth, mMapViewPortScreenHeight, mViewPortPadding));

                    Projection prj = mGoogleMap.getProjection();
                    LatLng dstCalcTarget = prj.fromScreenLocation(mViewPortScreenCenter);
                    float newZoom = mGoogleMap.getCameraPosition().zoom;

                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(currCamPos));

                    dstCamPosBuilder = new CameraPosition.Builder()
                            .target(dstCalcTarget)
                            .zoom(newZoom);

                    currentNavObj.gmapCameraPositionBuilder[0] = dstCamPosBuilder;

                } else {
                    dstCamPosBuilder = currentNavObj.gmapCameraPositionBuilder[0];
                }
            }

            if (animate) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(dstCamPosBuilder.build()), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        showDestinationInfoWindow(index);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            } else {
                mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(dstCamPosBuilder.build()));
                showDestinationInfoWindow(index);
            }
        }

        if (isOutDoorsStep && (currentNavObj.stepIndex >= 0)) {
            onStepSelected(currentNavObj.legIndex, currentNavObj.stepIndex, itemIndex);
        } else {
            onLegSelected(currentNavObj.legIndex, itemIndex);
        }

        scrollTo(itemIndex);

    }

    /**
     *
     * @param legIndex
     * @param currentRouteLeg
     * @param stepList
     * @param firstStep
     * @param didContextChange
     * @param isCurrentLegIndoors
     * @param legStepCount
     * @param isAction
     * @param inflater
     */
    @Override
    protected void addLegUI(int legIndex, RouteLeg currentRouteLeg, List<RouteStep> stepList, RouteStep firstStep, Boolean didContextChange, Boolean isCurrentLegIndoors, int legStepCount, Boolean isAction, LayoutInflater inflater) {

        View backgroundItem = inflater.inflate(R.layout.control_directions_menu_item_bg, null, true);
        View foregroundItem = inflater.inflate(R.layout.control_directions_menu_item_fg, null, true);

        mBackgroundLayout.addView(backgroundItem);
        mForegroundLayout.addView(foregroundItem);

        final int bgItemIndex = mBackgroundLayout.getChildCount() - 1;

        final int selItemIndex = mUINaviList.size();

        UIRouteNavigation naviObj = generateNaviListObj(currentRouteLeg, null, mIsLegIndoors);

        if (dbglog.isDeveloperMode()) {
            dbglog.Assert(naviObj != null, "UIRouteNavigation not created, reason: no points found in currentRouteLeg");
        }

        naviObj.set(mIsLegIndoors, legIndex, bgItemIndex);
        mUINaviList.add(naviObj);

        backgroundItem.setOnClickListener(v -> routeItemClickCallback(selItemIndex, true));

        View inlineView = backgroundItem.findViewById(R.id.dir_horiz_walk_inside_line);

        ImageView circleImageView = foregroundItem.findViewById(R.id.dir_horiz_circleImageView);
        TextView titleTextView = foregroundItem.findViewById(R.id.dir_horiz_titleTextView);

        inlineView.setVisibility(View.VISIBLE);

        setTravelModeIcon(firstStep, backgroundItem);

        if (mIsStarted) {
            String locationLabel = mOrigin.getLocationName(mActivity);
            String desc = mOrigin.getFormattedDetails(mActivity);

            locationLabel = String.format(Locale.US, "%s\n(%s)", locationLabel, desc);
            titleTextView.setText(locationLabel);
        }

        determineEnterOrExit(firstStep, didContextChange, isCurrentLegIndoors, titleTextView, circleImageView, null);

        if (isAction) {
            determineActionPoint(firstStep, stepList, titleTextView, legStepCount, circleImageView, null);
        } else if (!mIsStarted && !didContextChange) {
            determineActionPoint(firstStep, titleTextView);
        }
    }

    /**
     *
     * @param legIndex
     * @param stepIndex
     * @param previousTravelStep
     * @param stepList
     * @param inflater
     * @param didExitVenue
     */
    @Override
    protected void addTransitOutsideSteps(int legIndex, int stepIndex, RouteStep previousTravelStep, List<RouteStep> stepList, LayoutInflater inflater, boolean didExitVenue) {
        boolean travelModeIsTransit = mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_TRANSIT;

        LinearLayout backgroundLayout = mBackgroundLayout;
        LinearLayout foregroundLayout = mForegroundLayout;

        View backgroundItem = inflater.inflate(R.layout.control_directions_menu_item_bg, null, true); // RelativeLayout backgroundItem = (RelativeLayout)inflater.inflate(R.layout.control_directions_menu_item_bg, null, true);
        View foregroundItem = inflater.inflate(R.layout.control_directions_menu_item_fg, null, true); // RelativeLayout foregroundItem = (RelativeLayout)inflater.inflate(R.layout.control_directions_menu_item_fg, null, true);
        backgroundLayout.addView(backgroundItem);
        foregroundLayout.addView(foregroundItem);

        int bgItemIndex = backgroundLayout.getChildCount() - 1;

        final int selItemIndex = mUINaviList.size();

        UIRouteNavigation naviObj = generateNaviListObj(null, stepList, false);

        if (dbglog.isDeveloperMode()) {
            dbglog.Assert(naviObj != null, "UIRouteNavigation not created, reason: no points found in the stepList");
        }

        if (travelModeIsTransit) {
            naviObj.set(false, legIndex, stepIndex, bgItemIndex);
        } else {
            naviObj.set(false, legIndex, bgItemIndex);
        }

        mUINaviList.add(naviObj);

        backgroundItem.setOnClickListener(v -> routeItemClickCallback(selItemIndex, true));

        addTransitOutsideStepsUI(previousTravelStep, stepList, foregroundItem, backgroundItem, didExitVenue);
    }

    /**
     *
     * @param index
     */
    void showDestinationInfoWindow(int index) {
        MPLocation destinationLocation = mDestination.getLocation();
        if (destinationLocation != null) {
            if (index == mBackgroundLayout.getChildCount() - 1) {
                destinationLocation.showInfoWindow();
            } else {
                if (destinationLocation.isInfoWindowShown()) {
                    destinationLocation.hideInfoWindow();
                }
            }
        }
    }

    /**
     *
     * @param itemIndex
     */
    private void scrollTo(int itemIndex) {
        final HorizontalScrollView sv = mScrollingContainerLayout;

        ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);

        float scrollItemWidth = getResources().getDimension(R.dimen.directions_horiz_item_width);

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int winWidth = metrics.widthPixels;

        int scrollItemWidthInt = (int) scrollItemWidth;
        int destOffset = (winWidth - ((winWidth / scrollItemWidthInt) * scrollItemWidthInt)) >> 1;

        final int startPos = sv.getScrollX();
        final int destPos = (int) (scrollItemWidth * (itemIndex - 0.5f)) - destOffset;

        va.addUpdateListener(animation -> {
            float animVal = ((Float) animation.getAnimatedValue());
            float newPos = (startPos * (1 - animVal)) + (destPos * animVal);
            sv.scrollTo((int) newPos, sv.getBottom());
        });

        va.setDuration(500);
        va.start();
    }

    /**
     *
     * @param currRouteLeg
     * @param stepList
     * @param isPathIndoors
     * @return
     */
    UIRouteNavigation generateNaviListObj(RouteLeg currRouteLeg, List<RouteStep> stepList, boolean isPathIndoors) {
        List<RouteCoordinate> inputPointList = null;

        if (currRouteLeg != null) {
            inputPointList = currRouteLeg.getGeometry();
        } else if (stepList != null) {
            inputPointList = new ArrayList<>();

            for (RouteStep rs : stepList) {
                if (rs.getGeometry() != null) {
                    inputPointList.addAll(new ArrayList<>(rs.getGeometry()));
                }
            }
        }

        if (inputPointList == null) {
            dbglog.Assert(false, "either currRouteLeg or stepList are null");
            return null;
        }

        // The AABB of the leg/step, also when from a rotated indoors path's OBB
        final LatLngBounds latLngBounds;

        RouteCoordinate entry_p0, entry_p1;
        entry_p0 = entry_p1 = null;

        float entryHeadingAngle = 0;

        int pointCount = inputPointList.size();

        if (pointCount >= 2) {
            mTmpRCPointList.clear();

            if (isPathIndoors) {
                RouteCoordinate prevPnt = new RouteCoordinate(100, 100, 0);
                double prevLng, prevLat;
                prevLng = prevLat = 100;


                for (RouteCoordinate pnt : inputPointList) {
                    double lng = pnt.getLng();
                    double lat = pnt.getLat();

                    if ((Double.compare(lng, prevLng) != 0) && (Double.compare(lat, prevLat) != 0)) {
                        prevLng = lng;
                        prevLat = lat;

                        if ((pnt.distanceTo(prevPnt) < 0.5)) {
                            prevPnt = pnt;
                            continue;
                        }
                        prevPnt = pnt;

                        // Just need 2 points, for the entry points
                        mTmpRCPointList.add(pnt);
                        if (mTmpRCPointList.size() >= 2) {
                            break;
                        }
                    }
                }

                if (!mTmpRCPointList.isEmpty()) {
                    entry_p0 = mTmpRCPointList.get(0);

                    if (mTmpRCPointList.size() > 1) {
                        entry_p1 = mTmpRCPointList.get(1);
                    } else {
                        // Hack to get p1 from p0
                        entry_p1 = new RouteCoordinate(entry_p0.getLat() + 1, entry_p0.getLng(), 0);
                    }

                    double legHeading = SphericalUtil.computeHeading(entry_p0.getLatLng(), entry_p1.getLatLng());
                    entryHeadingAngle = (float) ((legHeading > 0) ? legHeading : (360.0 + legHeading));
                }


                mTmpRCPointList.clear();

                prevLng = prevLat = 100;

                for (RouteCoordinate pnt : inputPointList) {
                    double lng = pnt.getLng();
                    double lat = pnt.getLat();

                    if ((Double.compare(lng, prevLng) != 0) && (Double.compare(lat, prevLat) != 0)) {
                        prevLng = lng;
                        prevLat = lat;

                        mTmpRCPointList.add(pnt);
                    }
                }

                final LatLngBounds cBounds = MapsIndoorsHelper.getPathBounds(mTmpRCPointList);

                final LatLng pathCenter = SphericalUtil.interpolate(cBounds.northeast, cBounds.southwest, 0.5f);

                latLngBounds = MapsIndoorsHelper.getRotatedBoundsFromPath(mTmpRCPointList, pathCenter, -entryHeadingAngle);
            } else {
                latLngBounds = MapsIndoorsHelper.getPathBounds(inputPointList);
            }
        } else {
            latLngBounds = null;
        }

        return new UIRouteNavigation(entry_p0, entry_p1, latLngBounds, isPathIndoors, entryHeadingAngle);
    }

    /**
     *
     * @param routeListener
     */
    public void setDirectionViewSelectedStepListener(RouteListener routeListener) {
        this.mRouteListener = routeListener;
    }
}
