package com.mapsindoors.stdapp.ui.direction;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import com.mapsindoors.mapssdk.AgencyInfo;
import com.mapsindoors.mapssdk.Building;
import com.mapsindoors.mapssdk.Highway;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPRoutingProvider;
import com.mapsindoors.mapssdk.Maneuver;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnRouteResultListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.RoutingProvider;
import com.mapsindoors.mapssdk.UserRole;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.mapssdk.Route;
import com.mapsindoors.mapssdk.RouteLeg;
import com.mapsindoors.mapssdk.RouteStep;
import com.mapsindoors.mapssdk.TravelMode;

import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsRouteHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.helpers.SharedPrefsHelper;
import com.mapsindoors.stdapp.listeners.GenericObjectResultCallback;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.managers.UserRolesManager;
import com.mapsindoors.stdapp.models.LastRouteInfo;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.enums.DrawerState;
import com.mapsindoors.stdapp.ui.common.enums.MenuFrame;
import com.mapsindoors.stdapp.ui.common.listeners.RouteTrackingAdapter;
import com.mapsindoors.stdapp.ui.components.noInternetBar.NoInternetBar;
import com.mapsindoors.stdapp.ui.direction.models.RoutingEndPoint;
import com.mapsindoors.stdapp.ui.search.SearchFragment;
import com.mapsindoors.stdapp.ui.transportagencies.TransportAgenciesFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Fragment that searches for routes and shows a vertical Directions panel
 */
public class DirectionsVerticalFragment extends DirectionsFragment {
    static final String TAG = DirectionsVerticalFragment.class.getSimpleName();


    private static final int ROUTE_FLIPPER_INDEX = 0;
    private static final int NO_ROUTE_FLIPPER_INDEX = 1;
    private static final int LOADING_FLIPPER_INDEX = 2;

    private boolean isWaiting = false;

    boolean mIsNewRoute;

    private SearchFragment mSearchFragment;

    private TextView mFromTextView, mToTextView, mRouteTitleTextView, mShowTransportAgenciesButton;
    private ImageView mSrcDstFlipButton;
    private ImageView mVehicleWalkImageView;
    private ImageView mVehicleBicycleImageView;
    private ImageView mVehicleTransitImageView;
    private ImageView mVehicleCarImageView;
    private Switch mAvoidSwitchView;
    private ImageButton mBackButton;
    private Button mShowOnMapButton;
    private LinearLayout mRouteLayout;
    private ViewFlipper mMainViewFlipper;

    private DirectionsHorizontalFragment directionsHorizontalFragment;

    private NoInternetBar noInternetBar;

    private boolean isStepSelected = false;

    //region Fragment lifecycle events

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.fragment_directions_vertical, container);
        }
        return mMainView;
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mActivity != null) {
            final PositionProvider positionProvider = mActivity.getCurrentPositionProvider();
            if (positionProvider != null) {
                positionProvider.removeOnStateChangedListener(onPositionProviderStateChangedListener);
            }
        }

        mContext = null;
        mActivity = null;
        mMapControl = null;
    }

    //endregion

    /**
     *
     * @param view the view that is initialized in onViewCreated
     */
    @SuppressLint("SetTextI18n")
    protected void setupView(View view) {

        noInternetBar = view.findViewById(R.id.direction_frag_no_internet_message);

        noInternetBar.setOnClickListener(v -> {
            noInternetBar.setState(NoInternetBar.REFRESHING_STATE);
            updateList();
        });

        mMainViewFlipper = view.findViewById(R.id.direction_view_flipper);
        mBackButton = view.findViewById(R.id.directionsfullmenu_back_button);
        mShowOnMapButton = view.findViewById(R.id.showonmap);
        mAvoidSwitchView = view.findViewById(R.id.switchAvoidStairs);
        mRouteTitleTextView = view.findViewById(R.id.routeTitleTextView);

        mSrcDstFlipButton = view.findViewById(R.id.imageSwitchSrcDst);

        mFromTextView = view.findViewById(R.id.dir_full_textview_origin);
        mToTextView = view.findViewById(R.id.dir_full_textview_destination);

        mRouteLayout = view.findViewById(R.id.directions_full_route_items);

        mVehicleWalkImageView = view.findViewById(R.id.imageViewWalk);
        mVehicleBicycleImageView = view.findViewById(R.id.imageViewBicycle);
        mVehicleTransitImageView = view.findViewById(R.id.imageViewTransit);
        mVehicleCarImageView = view.findViewById(R.id.imageVehicleCar);
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

        LinearLayout foregroundItem = (LinearLayout) inflater.inflate(R.layout.control_directions_fullmenu_item, null, true);
        RelativeLayout mainLayout = foregroundItem.findViewById(R.id.dir_vert_itemRelativeLayout);
        mRouteLayout.addView(foregroundItem);

        final int bgItemIndex = mRouteLayout.getChildCount() - 1;

        mainLayout.setOnClickListener(view -> {
            selectRouteStep(bgItemIndex);
            routeNavigateToIndex(bgItemIndex, true);

        });

        ImageView circleImageView = foregroundItem.findViewById(R.id.circleImageView);
        TextView prefixTextView = foregroundItem.findViewById(R.id.prefixTextView);
        TextView titleTextView = foregroundItem.findViewById(R.id.titleTextView);
        View inlineView = foregroundItem.findViewById(R.id.walk_inside_line);

        TextView travelModeTextView = foregroundItem.findViewById(R.id.travelModeTextView);
        TextView distanceTextView = foregroundItem.findViewById(R.id.distanceTextView);

        travelModeTextView.setText(MapsIndoorsHelper.getTravelModeName(firstStep.getTravelModeVehicle()));

        setTravelModeIcon(firstStep, foregroundItem);

        directionUISetup(stepList, false, inflater, foregroundItem, distanceTextView);

        distanceTextView.setText(String.format("%s (%s)",
                MapsIndoorsRouteHelper.getFormattedDistance((int) currentRouteLeg.getDistance()),
                MapsIndoorsRouteHelper.getFormattedDuration((int) currentRouteLeg.getDuration())));

        inlineView.setVisibility(View.VISIBLE);


        if (mIsStarted) {

            prefixTextView.setText(mContext.getString(R.string.prefix_start));

            titleTextView.setText(formatPOIText(mOrigin, false));

        }

        determineEnterOrExit(firstStep, didContextChange, isCurrentLegIndoors, titleTextView, circleImageView, prefixTextView);

        if (isAction) {
            determineActionPoint(firstStep, stepList, titleTextView, legStepCount, circleImageView, prefixTextView);
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

        LinearLayout foregroundItem = (LinearLayout) inflater.inflate(R.layout.control_directions_fullmenu_item, null, true);
        RelativeLayout mainLayout = foregroundItem.findViewById(R.id.dir_vert_itemRelativeLayout);

        mRouteLayout.addView(foregroundItem);

        final int bgItemIndex = mRouteLayout.getChildCount() - 1;

        mainLayout.setOnClickListener(v -> routeNavigateToIndex(bgItemIndex, true));

        TextView distanceTextView = foregroundItem.findViewById(R.id.distanceTextView);

        directionUISetup(stepList,travelModeIsTransit,inflater,foregroundItem,distanceTextView);

        addTransitOutsideStepsUI(previousTravelStep, stepList, foregroundItem, null, didExitVenue);
    }

    /**
     *
     * @param stepList
     * @param travelModeIsTransit
     * @param inflater
     * @param item
     * @param distanceTextView
     */
    void directionUISetup(List<RouteStep> stepList, boolean travelModeIsTransit, LayoutInflater inflater, View item, TextView distanceTextView) {

        final ImageView directionArrowImageView = item.findViewById(R.id.directionArrowImageView);
        final LinearLayout directionsLinearLayout = item.findViewById(R.id.directionsLinearLayout);
        final LinearLayout directionTitleLinearLayout = item.findViewById(R.id.directionTitleLinearLayout);


        directionTitleLinearLayout.setOnClickListener(view -> {
            if (directionsLinearLayout.getVisibility() == View.GONE) {
                directionsLinearLayout.setVisibility(View.VISIBLE);
                directionArrowImageView.setImageResource(R.drawable.ic_expand_less);
            } else {
                directionsLinearLayout.setVisibility(View.GONE);
                directionArrowImageView.setImageResource(R.drawable.ic_expand_more);
            }
        });

        directionTitleLinearLayout.setVisibility(View.VISIBLE);

        float distance, duration;
        distance = duration = 0f;
        RouteStep firstStep = stepList.get(0);

        final List<RouteStep> directionsList = !travelModeIsTransit ? stepList : firstStep.getSteps();

        if (directionsList != null) {

            for (RouteStep step: directionsList) {
                distance += step.getDistance();
                duration += step.getDuration();

                LinearLayout directionItem = (LinearLayout) inflater.inflate(R.layout.control_directions_item, null, true);
                directionsLinearLayout.addView(directionItem);
                ImageView directionImageView = directionItem.findViewById(R.id.directionImageView);
                TextView directionTitleTextView = directionItem.findViewById(R.id.directionTitleTextView);
                TextView directionDistanceTextView = directionItem.findViewById(R.id.directionDistanceTextView);

                String maneuver = step.getManeuver();
                String highWay = step.getHighway();

                if (maneuver != null) {
                    maneuver = maneuver.toLowerCase(Locale.ROOT);
                }

                if ((maneuver == null) || !MapsIndoorsHelper.hasManeuverIcon(maneuver)) {
                    directionImageView.setImageResource(MapsIndoorsHelper.getManeuverIcon(Maneuver.STRAIGHT_AHEAD));

                    String htmlInstructions = step.getHtmlInstructions();

                    if (htmlInstructions == null) {
                        directionTitleTextView.setText(getResources().getString(R.string.continue_));
                    } else {
                        String cleanedUpHtmlInstructions = htmlInstructions.replaceAll("[<]div[^>]*[>]", "<br>").replaceAll("[<](/)+div[^>]*[>]", "");
                        directionTitleTextView.setText(MapsIndoorsUtils.fromHtml(cleanedUpHtmlInstructions));
                    }
                } else {

                    boolean maneuverIsStraightViaSteps = highWay.equalsIgnoreCase(Highway.STEPS);
                    maneuver = !maneuverIsStraightViaSteps ? maneuver : Maneuver.STRAIGHT_AHEAD_VIA_STAIRS;

                    directionImageView.setImageResource(MapsIndoorsHelper.getManeuverIcon(maneuver));

                    String htmlInstructions = step.getHtmlInstructions();

                    if (htmlInstructions == null) {
                        directionTitleTextView.setText(MapsIndoorsUtils.fromHtml(MapsIndoorsHelper.getManeuverInstructions(maneuver)));
                    } else {

                        String cleanedUpHtmlInstructions = htmlInstructions.replaceAll("[<]div[^>]*[>]", "<br>").replaceAll("[<](/)+div[^>]*[>]", "");
                        directionTitleTextView.setText(MapsIndoorsUtils.fromHtml(cleanedUpHtmlInstructions));
                    }
                }
                directionDistanceTextView.setText(String.format("%s", MapsIndoorsRouteHelper.getFormattedDistance((int) step.getDistance())));
            }
        }

        distanceTextView.setText(String.format("%s (%s)", MapsIndoorsRouteHelper.getFormattedDistance((int) distance), MapsIndoorsRouteHelper.getFormattedDuration((int) duration)));
    }

    /**
     *
     */
    interface WhateverIsReadyListener {
        void okImDone();
    }

    /**
     *
     * @param context
     * @param mapControl
     */
    public void init(final Context context, final MapControl mapControl) {
        mContext = context;
        mActivity = (MapsIndoorsActivity) context;
        mMapControl = mapControl;

        MapsIndoorsHelper.init(mActivity);

        // do not reset because another call for the updateList will be done in the open() method
        resetUI(false);

        initVehicleSelector();

        mBackButton.setOnClickListener((view) -> close());

        disableShowOnMapButton();

        mShowOnMapButton.setOnClickListener(view -> showRouteOnMap(0));

        final boolean avoidStairsSwitchSet = SharedPrefsHelper.getAvoidStairs(mContext);
        mAvoidSwitchView.setChecked(avoidStairsSwitchSet);

        mAvoidSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPrefsHelper.setAvoidStairs(mContext, b);
            updateList();

            final Bundle eventParams = new Bundle();
            eventParams.putString(getString(R.string.fir_param_Avoid_stairs), mAvoidSwitchView.isChecked() ? "true" : "false");
            GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Avoid_Stairs_Clicked), eventParams);

        });

        mSrcDstFlipButton.setOnClickListener(swapButtonClickListener);

        mSearchFragment = mActivity.getDirectionsFullMenuSearchFragment();
        mSearchFragment.init(context, mapControl);

        mFromTextView.setOnClickListener(fromButtonOnClickListener);
        mToTextView.setOnClickListener(toButtonOnClickListener);

        directionsHorizontalFragment = mActivity.getHorizontalDirectionsFragment();

        directionsHorizontalFragment.setDirectionViewSelectedStepListener(this::selectRouteStep);

        mActivity.getUserPositionTrackingViewModel().setRouteTrackingAdapter(new RouteTrackingAdapter() {
            @Override
            public void routeSegmentPathChanged(Route route, int legIndex, int stepIndex) {
            }

            @Override
            public void selectRouteLeg(Route route, int legIndex) {
                if (legIndex != selectedLeg) {
                    showRouteOnMap(legIndex);
                    selectRouteStep(legIndex);
                    selectedLeg = legIndex;
                }
            }
        });

        final PositionProvider positionProvider = mActivity.getCurrentPositionProvider();

        if (positionProvider != null) {
            positionProvider.addOnStateChangedListener(onPositionProviderStateChangedListener);
        }
    }

    /**
     *
     */
    private void resetVars() {
        mCurrentRoute = null;
        mIsNewRoute = true;
        isStepSelected = false;
    }

    /**
     *
     */
    void updateTextViews() {
        if (getOrigin() != null) {
            mFromTextView.setText(formatPOIText(getOrigin(), false));
        } else {
            mFromTextView.setText("");
        }

        if (getDestination() != null) {
            mToTextView.setText(formatPOIText(getDestination(), true));
        } else {
            mToTextView.setText("");
        }
    }

    /**
     *
     * @param route
     * @param legIndexToSelect
     */
    void renderRouteOnMap(Route route, int legIndexToSelect) {

        mActivity.setToolbarTitle(getContext().getResources().getString(R.string.toolbar_label_directions), true);
        //First close the routing from the vertical fragment menu. The horizontal fragment will show the route now.
        clearCurrentRoute();

        DirectionsHorizontalFragment horizontalDirectionsFragment = mActivity.getHorizontalDirectionsFragment();
        if (horizontalDirectionsFragment == null) {
            return;
        }

        horizontalDirectionsFragment.setStartEndLocations(getOrigin(), getDestination());
        horizontalDirectionsFragment.setCurrentTravelMode(getSelectedTravelMode());

        horizontalDirectionsFragment.renderRoute(
                route,
                legIndexToSelect
        );
    }

    /**
     *
     * @param origin
     * @param destination
     */
    void updateRouteEndpointsFromSwap(@NonNull RoutingEndPoint origin, @NonNull RoutingEndPoint destination) {
        final DirectionsHorizontalFragment horizontalDirectionsFragment = mActivity.getHorizontalDirectionsFragment();
        if (horizontalDirectionsFragment == null) {
            return;
        }

        horizontalDirectionsFragment.setStartEndLocations(origin, destination);
    }

    void showRouteOnMap(int legIndexToSelect) {
        selectedLeg = legIndexToSelect;

        boolean isDirectionsMenuFragmentActive = !directionsHorizontalFragment.isActive();
        if (mIsNewRoute || !isDirectionsMenuFragmentActive) {

            renderRouteOnMap(mCurrentRoute, legIndexToSelect);

            new Handler(mContext.getMainLooper()).postDelayed(() -> ((MapsIndoorsActivity) mContext).closeDrawer(), 250);
        } else {
            mActivity.closeDrawer();
            directionsHorizontalFragment.routeNavigateToIndex(legIndexToSelect, true);
        }
    }

    /**
     * Clears the route list, calculate a new route and show that.
     */
    void updateList() {
        // these 2 lines prevent the mScrollView to stay at the same size than it was in the last route
        mRouteLayout.getLayoutParams().height = 0;
        mRouteLayout.requestLayout();

        if ((getOrigin() == null) || (getDestination() == null)) {
            return;
        }

        // in this state of the app it's only when the position of the user changed it will get updated
        refreshEndpoints(() -> {
            new Handler(mContext.getMainLooper()).post(() -> {
                resetDirectionViewLegs();

                updateTextViews();

                changeWaitStatus(true, ROUTE_FLIPPER_INDEX);

                final String[] avoid = avoidStairs() ? new String[]{Highway.STEPS, Highway.LADDER} : new String[]{};

                route(
                        getOrigin().getLocation(),
                        getDestination().getLocation(),
                        getSelectedTravelMode(),
                        avoid,
                        0,
                        0,
                        null
                );
            });
        });
    }

    boolean refreshOrigin, refreshDestination;

    /**
     *
     * @param taskDone
     */
    void refreshEndpoints(TaskDoneListener taskDone) {

        refreshOrigin = refreshDestination = false;

        refreshRoutingEndPoint(getDestination(), rEndpoint -> {
            setDestination(rEndpoint);

            refreshDestination = true;

            if (refreshOrigin) {
                taskDone.done();
            }
        });

        refreshRoutingEndPoint(getOrigin(), rEndpoint -> {
            setOrigin(rEndpoint);

            refreshOrigin = true;
            if (refreshDestination) {
                taskDone.done();
            }
        });
    }

    /**
     *
     */
    interface TaskDoneListener {
        void done();
    }

    /**
     * Refreshes the endpoints of the Route, if the user is on the Route one of the end points are replaced with the users position.
     * @param currentRouteEndpoint
     * @param resultCallback
     */
    private void refreshRoutingEndPoint(RoutingEndPoint currentRouteEndpoint, GenericObjectResultCallback<RoutingEndPoint> resultCallback) {
        if (currentRouteEndpoint.getLocation() == null || currentRouteEndpoint.getType() == RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING || currentRouteEndpoint.getType() == RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_OUTSIDE_BUILDING) {
            getMyPositionRoutingEndpoint(resultCallback);
        } else {
            resultCallback.onResultReady(currentRouteEndpoint);
        }
    }

    /**
     *
     * @param route
     */
    void updateList(@Nullable final Route route) {
        if (mActivity == null) {
            return;
        }

        mActivity.runOnUiThread(() -> {

            if ((getOrigin() == null) || (getDestination() == null)) {
                return;
            }

            resetDirectionViewLegs();

            updateTextViews();

            changeWaitStatus(true, ROUTE_FLIPPER_INDEX);

            clearCurrentRoute();

            mIsNewRoute = true;

            if (route != null) {
                render(route);
                mCurrentRoute = route;

                enableShowOnMapButton();

                changeWaitStatus(false, ROUTE_FLIPPER_INDEX);
            } else {
                changeWaitStatus(false, NO_ROUTE_FLIPPER_INDEX);
            }
        });
    }

    /**
     * Swaps the Origin and destination of the Route
     */
    void swapLocations() {

        RoutingEndPoint swap = getOrigin();

        setOrigin(getDestination());

        setDestination(swap);

        mSearchFragment.swapDirectionSearchData();

        updateTextViews();
        updateRouteEndpointsFromSwap(mOrigin, mDestination);
    }

    /**
     *
     * @param newLocation
     */
    void setOrigin(RoutingEndPoint newLocation) {
        mOrigin = newLocation;
    }

    /**
     *
     * @param newLocation
     */
    void setDestination(RoutingEndPoint newLocation) {
        mDestination = newLocation;
    }

    /**
     *
     * @return The origin of the Route
     */
    @Nullable
    final RoutingEndPoint getOrigin() {
        return mOrigin;
    }

    /**
     *
     * @return The destination of the Route
     */
    @Nullable
    final RoutingEndPoint getDestination() {
        return mDestination;
    }

    /**
     *
     * @param avoidStairs
     */
    void setAvoidStairs(boolean avoidStairs) {
        mAvoidSwitchView.setChecked(avoidStairs);
    }

    /**
     *
     * @return
     */
    boolean avoidStairs() {
        return mAvoidSwitchView.isChecked();
    }

    /**
     *
     * @param destination
     */
    public void open(@NonNull MPLocation destination) {
        open(destination, null, null, null);
    }

    /**
     *
     * @param destination
     * @param origin
     * @param travelMode
     * @param avoid
     */
    public void open(@NonNull MPLocation destination, @Nullable MPLocation origin, @Nullable String travelMode, @Nullable String avoid) {
        mActivity.setOpenLocationMenuFromInfowindowClick(false);

        resetUI(false);

        LastRouteInfo lastRouteInfo = mActivity.getLastRouteInfo();
        Route prevCalcRoute = mActivity.getLastRoute();

        changeRouteLayoutState(ROUTE_FLIPPER_INDEX);

        final RoutingEndPoint destinationEndpoint = new RoutingEndPoint(destination, "", RoutingEndPoint.ENDPOINT_TYPE_POI);

        setDestination(destinationEndpoint);

        if (travelMode != null) {
            setSelectedTravelMode(travelMode);
        }

        if (avoid != null) {
            boolean shouldAvoidStairs = avoid.equalsIgnoreCase("stairs");
            setAvoidStairs(shouldAvoidStairs);
        }

        if (origin != null) {
            RoutingEndPoint originEndpoint = new RoutingEndPoint(origin, "", RoutingEndPoint.ENDPOINT_TYPE_POI);
            setOrigin(originEndpoint);
        }

        updateTextViews();
        updateVehicleSelector();

        final boolean showPreCalcRoute = getResources().getBoolean(R.bool.default_starting_position_enabled);
        final boolean hasPrevCalcRoute = prevCalcRoute != null;

        if (showPreCalcRoute && hasPrevCalcRoute) {
            MPLocation prevCalcRouteOriginLocation;
            prevCalcRouteOriginLocation = mActivity.getLastRouteOrigin();

            if (lastRouteInfo.isOriginInsideABuilding()) {

                RoutingEndPoint originEndpoint = new RoutingEndPoint(prevCalcRouteOriginLocation, "", RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING);

                setOrigin(originEndpoint);
                double distance = getOrigin().getLocation().getPoint().distanceTo(getDestination().getLocation().getPoint());

                if (SharedPrefsHelper.getUserTravelingMode(getContext()) == MapsIndoorsHelper.VEHICLE_NONE) {
                    if (distance > MapsIndoorsSettings.ROUTING_MAX_WALKING_DISTANCE_IN_METERS) {
                        mSelectedTravelMode = MapsIndoorsHelper.VEHICLE_DRIVING;
                    }
                } else {
                    mSelectedTravelMode = SharedPrefsHelper.getUserTravelingMode(getContext());
                }

                updateTextViews();
                updateList(prevCalcRoute);
            } else {
                MapsIndoorsHelper.getGooglePlacesAddressByPosition(getContext(), mActivity.getCurrentUserPos(), text -> {
                    final String desc = (text != null) ? text : "";

                    RoutingEndPoint originEndpoint = new RoutingEndPoint(prevCalcRouteOriginLocation, desc, RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_OUTSIDE_BUILDING);

                    setOrigin(originEndpoint);

                    double distance = getOrigin().getLocation().getPoint().distanceTo(getDestination().getLocation().getPoint());

                    if (SharedPrefsHelper.getUserTravelingMode(getContext()) == MapsIndoorsHelper.VEHICLE_NONE) {
                        if (distance > MapsIndoorsSettings.ROUTING_MAX_WALKING_DISTANCE_IN_METERS) {
                            mSelectedTravelMode = MapsIndoorsHelper.VEHICLE_DRIVING;
                        }
                    } else {
                        mSelectedTravelMode = SharedPrefsHelper.getUserTravelingMode(getContext());
                    }

                    updateTextViews();
                    updateList(prevCalcRoute);
                });
            }

            updateVehicleSelector();

            updateList(prevCalcRoute);

            enableShowOnMapButton();
        } else {
            updateList();
        }

        // Change the top field text to 'search for <name of location>'
        mActivity.menuGoTo(MenuFrame.MENU_FRAME_DIRECTIONS_FULL_MENU, true);
    }

    /**
     *
     */
    public void close() {
        mActivity.setOpenLocationMenuFromInfowindowClick(true);

        closeRouting();

        mActivity.resetMapToInitialState();

        mActivity.menuGoBack();

        noInternetBar.setVisibility(View.GONE);
    }


    WhateverIsReadyListener mWhateverIsReadyListener;
    RoutingProvider routingProvider;

    /**
     *
     * @param origin        The origin of the route
     * @param destination   The destination of the route
     * @param travelMode    The method of travel, if set to TRANSIT departure and arrival is used
     * @param avoids        A list of opstacles to avoid
     * @param departure     Set to zero to use the current time, or to a epoch time (in seconds)
     * @param arrival       Set to zero to use the current time, or to a epoch time (in seconds). If both departure and arrival are set, arrival is used
     * @param doneListener
     */
    void route(final MPLocation origin, final MPLocation destination, final String travelMode, String[] avoids, int departure, int arrival, WhateverIsReadyListener doneListener) {
        mWhateverIsReadyListener = doneListener;

        changeWaitStatus(true, ROUTE_FLIPPER_INDEX);

        clearCurrentRoute();

        mRouteTitleTextView.setText("");

        disableShowOnMapButton();

        if (origin == null || destination == null) {
            return;
        }

        if (routingProvider != null) {
            routingProvider.setOnRouteResultListener(null);
        }
        routingProvider = new MPRoutingProvider();

        routingProvider.setLanguage(MapsIndoors.getLanguage());

        routingProvider.setOnRouteResultListener(mOnRouteResultListener);
        routingProvider.setTravelMode(travelMode);
        routingProvider.clearRouteRestrictions();

        if (avoids != null) {
            for (String avoid : avoids) {
                routingProvider.addRouteRestriction(avoid);
            }
        }

        if (travelMode.equalsIgnoreCase(TravelMode.TRANSIT)) {
            if (arrival > 0) {
                routingProvider.setDateTime(arrival, false);
            } else if (departure > 0) {
                routingProvider.setDateTime(departure, true);
            }
        }

        final Point originPoint = origin.getPoint();
        final Point destinationPoint = destination.getPoint();

        String logMsg = "Origin: " + originPoint.getCoordinatesAsString() + "/Destination: " + destinationPoint.getCoordinatesAsString() + "/" + "TravelMode: " + travelMode;
        FirebaseCrashlytics.getInstance().log(logMsg);

        {
            final List<UserRole> savedUserRoles;
            final UserRolesManager userRolesManager = mActivity.getUserRolesManager();
            if (userRolesManager != null) {
                savedUserRoles = userRolesManager.getSavedUserRoles();
            } else {
                savedUserRoles = null;
            }

            routingProvider.setUserRoles(savedUserRoles);
        }

        routingProvider.query(getOrigin().getLocation().getPoint(), getDestination().getLocation().getPoint());
    }

    /**
     * Close the current routing control.
     */
    public void closeRouting() {
        clearCurrentRoute();

        resetDirectionViewLegs();

        if (directionsHorizontalFragment != null) {
            directionsHorizontalFragment.setActive(false);
        }
        resetVars();

        mMapControl.setMapPadding(0, 0, 0, 0);
        mActivity.setFollowMeBtnBottomMarginToDefault();
        mActivity.getUserPositionTrackingViewModel().setRoute(null);

        if (mSearchFragment != null) {
            mSearchFragment.clearDirectionSearchData();
        }
    }

    /**
     *
     */
    void clearCurrentRoute() {
        if (directionsHorizontalFragment != null) {

            directionsHorizontalFragment.clearRoute();
        }
    }

    /**
     *
     * @param doUpdateList
     */
    private void resetUI(boolean doUpdateList) {
        mOrigin = null;
        mDestination = null;
        mFromTextView.setText("");
        mToTextView.setText("");
        mRouteTitleTextView.setText("");

        if (doUpdateList) {
            updateList();
        }

        mTempRouteStepsList.clear();
        mTransportAgencies.clear();

        disableShowOnMapButton();
    }

    /**
     * Populate the directions view with the legs from a route
     *
     */
    void render(@Nullable Route route) {
        resetDirectionViewLegs();

        List<RouteLeg> legs = route.getLegs();

        mIsLegIndoors = MapsIndoorsHelper.isStepInsideBuilding(legs.get(0).getSteps().get(0));

        float routeDistance, routeDuration;
        routeDistance = routeDuration = 0.0f;
        mTransportAgencies.clear();

        int i = 0;
        int firstStepTravelMode = legs.get(0).getSteps().get(0).getTravelModeVehicle();

        boolean sameTravelMode = true;

        for (RouteLeg currentRouteLeg : legs) {

            routeDistance += currentRouteLeg.getDistance();
            routeDuration += currentRouteLeg.getDuration();

            //
            addLeg(currentRouteLeg, i++);

            for (RouteStep step : currentRouteLeg.getSteps()) {

                if (step.getTravelModeVehicle() != firstStepTravelMode) {
                    sameTravelMode = false;
                }
            }

        }

        int selectedTravelMode = sameTravelMode ? firstStepTravelMode : mSelectedTravelMode;

        String travelModeText;
        switch (selectedTravelMode) {

            case TravelMode.VEHICLE_BICYCLING:
                travelModeText = getString(R.string.travel_mode_by_bike);
                break;
            case TravelMode.VEHICLE_DRIVING:
                travelModeText = getString(R.string.travel_mode_by_car);
                break;

            case TravelMode.VEHICLE_TRANSIT:
                travelModeText = getString(R.string.travel_mode_by_transit);
                break;

            case TravelMode.VEHICLE_WALKING:
                travelModeText = getString(R.string.travel_mode_by_walk);
                break;
            case TravelMode.VEHICLE_NONE:
            default:
                travelModeText = "";
        }

        mRouteTitleTextView.setText(String.format(
                getString(R.string.direction_time_to_destination),
                MapsIndoorsRouteHelper.getFormattedDuration((int) routeDuration),
                travelModeText,
                MapsIndoorsRouteHelper.getFormattedDistance((int) routeDistance)
        ));


        LayoutInflater inflater = LayoutInflater.from(mContext);

        View foregroundItem = inflater.inflate(R.layout.control_directions_fullmenu_item, null, true);
        mRouteLayout.addView(foregroundItem);

        ImageView circleImageView = foregroundItem.findViewById(R.id.circleImageView);
        TextView prefixTextView = foregroundItem.findViewById(R.id.prefixTextView);
        TextView titleTextView = foregroundItem.findViewById(R.id.titleTextView);
        RelativeLayout itemRelativeLayout = foregroundItem.findViewById(R.id.dir_vert_itemRelativeLayout);
        prefixTextView.setText(mContext.getString(R.string.prefix_end));

        circleImageView.setBackgroundResource(R.drawable.ic_direction_end_marker);

        titleTextView.setText(formatPOIText(mDestination, false));


        itemRelativeLayout.setVisibility(View.GONE);

        renderRouteOnMap(route, 0);


        // Check the "agencies" chapter in the design document
        if (mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_TRANSIT) {
            int transportAgenciesCount = mTransportAgencies.size();
            if (transportAgenciesCount > 0) {
                prepareTransportAgenciesList(mTransportAgencies);
                transportAgenciesCount = mTransportAgencies.size();

                View transportAgencies = inflater.inflate(R.layout.control_directions_fullmenu_item_transport_sources, null, true);
                mShowTransportAgenciesButton = transportAgencies.findViewById(R.id.dir_full_transp_sources_text);
                mShowTransportAgenciesButton.setText(MapsIndoorsUtils.fromHtml(String.format(getString(R.string.transport_agencies_sources), transportAgenciesCount)));

                mRouteLayout.addView(transportAgencies);

                TransportAgenciesFragment transportAgenciesFrag = mActivity.getTransportAgenciesFragment();
                if (transportAgenciesFrag != null) {
                    transportAgenciesFrag.setList(mTransportAgencies);
                }

                mShowTransportAgenciesButton.setOnClickListener(view -> {
                    ((MapsIndoorsActivity) mContext).menuGoTo(MenuFrame.MENU_FRAME_TRANSPORT_AGENCIES, true);
                });
            }
        }

        changeRouteLayoutState(ROUTE_FLIPPER_INDEX);

        mActivity.onRouteRendered();
    }

    /**
     * Removed all legs from the view leaving it blank.
     */
    public void resetDirectionViewLegs() {
        mRouteLayout.removeAllViewsInLayout();
        mRouteLayout.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    /**
     *
     * @param isWaiting
     * @param toShowViewFlipperIndex
     */
    void changeWaitStatus(final boolean isWaiting, int toShowViewFlipperIndex) {
        if (mActivity == null) {
            return;
        }

        if (isWaiting != this.isWaiting) {

            this.isWaiting = isWaiting;

            if (isWaiting) {
                changeRouteLayoutState(LOADING_FLIPPER_INDEX);
            } else {
                changeRouteLayoutState(toShowViewFlipperIndex);
            }

            mActivity.runOnUiThread(() -> {

                final ProgressBar waitingProgressBar = mMainView.findViewById(R.id.directions_fullmenu_workingprogressbar);
                ValueAnimator va = isWaiting ? ValueAnimator.ofFloat(0f, 1f) : ValueAnimator.ofFloat(1f, 0f);
                va.addUpdateListener(animation -> {
                    float animVal = ((Float) animation.getAnimatedValue());
                    waitingProgressBar.setAlpha(animVal);
                });
                va.start();
            });
        }
    }


    //region VehicleSelector

    /**
     *
     */
    private void initVehicleSelector() {
        mVehicleWalkImageView.setOnClickListener(mVehicleSelectorOnClickListener);
        mVehicleBicycleImageView.setOnClickListener(mVehicleSelectorOnClickListener);
        mVehicleTransitImageView.setOnClickListener(mVehicleSelectorOnClickListener);
        mVehicleCarImageView.setOnClickListener(mVehicleSelectorOnClickListener);
    }

    /**
     *
     */
    void updateVehicleSelector() {
        mVehicleWalkImageView.setAlpha(mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_WALKING ? 1.0f : 0.5f);
        mVehicleBicycleImageView.setAlpha(mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_BICYCLING ? 1.0f : 0.5f);
        mVehicleTransitImageView.setAlpha(mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_TRANSIT ? 1.0f : 0.5f);
        mVehicleCarImageView.setAlpha(mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_DRIVING ? 1.0f : 0.5f);

        SharedPrefsHelper.setUserTravelingMode(mContext, mSelectedTravelMode);
    }

    final View.OnClickListener mVehicleSelectorOnClickListener = view -> {

        @MapsIndoorsHelper.Vehicle int selTravelMode = 0;

        int id = view.getId();


        if (id == R.id.imageViewWalk) {
            selTravelMode = MapsIndoorsHelper.VEHICLE_WALKING;
        } else if (id == R.id.imageViewBicycle) {
            selTravelMode = MapsIndoorsHelper.VEHICLE_BICYCLING;
        } else if (id == R.id.imageViewTransit) {
            selTravelMode = MapsIndoorsHelper.VEHICLE_TRANSIT;
        } else if (id == R.id.imageVehicleCar) {
            selTravelMode = MapsIndoorsHelper.VEHICLE_DRIVING;
        }

        if (selTravelMode != mSelectedTravelMode) {
            mSelectedTravelMode = selTravelMode;
            vehicleSelectorClicked();
        }
    };

    /**
     *
     */
    void vehicleSelectorClicked() {
        updateVehicleSelector();
        updateList();
        reportTravelModeToAnalytics();
    }

    /**
     *
     */
    void reportTravelModeToAnalytics() {
        final Bundle eventParams = new Bundle();

        eventParams.putString(getString(R.string.fir_param_Travel_Mode), getSelectedTravelMode());

        GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Travel_Mode_Selected), eventParams);
    }

    /**
     *
     * @return
     */
    String getSelectedTravelMode() {
        switch (mSelectedTravelMode) {
            case MapsIndoorsHelper.VEHICLE_BICYCLING:
                return TravelMode.BICYCLING;
            case MapsIndoorsHelper.VEHICLE_DRIVING:
                return TravelMode.DRIVING;
            case MapsIndoorsHelper.VEHICLE_TRANSIT:
                return TravelMode.TRANSIT;
            case MapsIndoorsHelper.VEHICLE_WALKING:
            case MapsIndoorsHelper.VEHICLE_NONE:
            default:
                return TravelMode.WALKING;
        }
    }

    /**
     *
     * @param travelMode
     */
    void setSelectedTravelMode(String travelMode) {
        if (travelMode == null) {
            return;
        }

        switch (travelMode.toUpperCase()) {
            case TravelMode.BICYCLING:
                mSelectedTravelMode = MapsIndoorsHelper.VEHICLE_BICYCLING;
                break;
            case TravelMode.DRIVING:
                mSelectedTravelMode = MapsIndoorsHelper.VEHICLE_DRIVING;
                break;
            case TravelMode.TRANSIT:
                mSelectedTravelMode = MapsIndoorsHelper.VEHICLE_TRANSIT;
                break;
            case TravelMode.WALKING:
            default:
                mSelectedTravelMode = MapsIndoorsHelper.VEHICLE_WALKING;
                break;
        }

        updateVehicleSelector();
    }
    //endregion

    /**
     *
     * @return
     */
    @Override
    public boolean onBackPressed() {
        if (isActive()) {
            if (!((MapsIndoorsActivity) mContext).isDrawerOpen()) {
                mActivity.openDrawer(true);
            } else {
                close();
            }
        }

        return true;
    }

    /**
     *
     * @param newState
     * @param prevState
     */
    @Override
    public void onDrawerEvent(DrawerState newState, DrawerState prevState) {
        switch (newState) {
            case DRAWER_ISTATE_IS_OPEN:
                if (mCurrentRoute != null && directionsHorizontalFragment.isActive()) {
                    directionsHorizontalFragment.setShow(false);
                }
                break;

            case DRAWER_ISTATE_IS_CLOSED:
                if (mCurrentRoute != null && isStepSelected && isActive()) {
                    directionsHorizontalFragment.setShow(true);
                }
                break;
        }
    }

    final OnStateChangedListener onPositionProviderStateChangedListener = isEnabled -> updateList();

    /**
     *
     */
    void disableShowOnMapButton() {
        mShowOnMapButton.setAlpha(.5f);
        mShowOnMapButton.setEnabled(false);
    }

    /**
     *
     */
    void enableShowOnMapButton() {
        mShowOnMapButton.setAlpha(1);
        mShowOnMapButton.setEnabled(true);
    }

    /**
     *
     * @param list
     */
    private void prepareTransportAgenciesList(List<AgencyInfo> list) {

        Collections.sort(list, (a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));

        List<AgencyInfo> toBeRemoved = new ArrayList<>(list.size());
        String pAgencyName, pAgencyUrl;
        pAgencyName = pAgencyUrl = "";

        for (AgencyInfo agencyInfo : list) {
            if ((agencyInfo.getName().equals(pAgencyName)) && (agencyInfo.getUrl().equals(pAgencyUrl))) {
                toBeRemoved.add(agencyInfo);
            }

            pAgencyName = agencyInfo.getName();
            pAgencyUrl = agencyInfo.getUrl();
        }
        list.removeAll(toBeRemoved);
    }

    /**
     *
     * @param index the index of the route
     * @param animate whether the route should be animated, not used here, but is required in the other directional fragment
     */
    @Override
    public void routeNavigateToIndex(int index, boolean animate) {
        mActivity.getUserPositionTrackingViewModel().stopTracking();

        showRouteOnMap(index);
    }

    /**
     *
     * @param index
     */
    void changeRouteLayoutState(int index) {
        mMainViewFlipper.setDisplayedChild(index);
    }

    /**
     *
     * @param stepIndex
     */
    void selectRouteStep(int stepIndex) {
        isStepSelected = true;

        int routeChildCount = mRouteLayout.getChildCount();
        if ((stepIndex >= 0) && (stepIndex < routeChildCount)) {
            for (int i = 0; i < routeChildCount; i++) {
                mRouteLayout.getChildAt(i)
                        .setBackgroundColor((i != stepIndex)
                                ? Color.TRANSPARENT
                                : ContextCompat.getColor(mContext, R.color.dir_panel_selected_step));
            }
        }
    }

    private int selectedLeg = -1;

    /**
     *
     */
    OnRouteResultListener mOnRouteResultListener = new OnRouteResultListener() {
        @Override
        public void onRouteResult(@Nullable final Route route, @Nullable MIError error) {
            if (mActivity == null) {
                return;
            }

            if (error != null) {
                if (error.code == MIError.INVALID_API_KEY) {
                    MapsIndoorsUtils.showInvalidAPIKeyDialogue(getContext());
                }
            }

            mActivity.runOnUiThread(() -> {
                boolean foundRoute = route != null;

                // Assume that we've got a new route when in here...
                mIsNewRoute = true;


                if (BuildConfig.DEBUG) {
                    dbglog.Log(TAG, "DirectionsVerticalFragment.route() -> onRouteResult: route=" + route + ", error=" + error);
                }

                if (foundRoute) {

                    mCurrentRoute = route;

                    mActivity.getUserPositionTrackingViewModel().setRoute(mCurrentRoute);

                    noInternetBar.setVisibility(View.GONE);

                    {
                        final Bundle eventParams = new Bundle();
                        eventParams.putString("Origin", mOrigin.getLocationName(mActivity));
                        eventParams.putString("Destination", mDestination.getLocationName(mActivity));
                        eventParams.putLong("Distance", route.getDistance());

                        GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_route_calculated), eventParams);
                    }

                    render(route);

                    enableShowOnMapButton();

                    changeWaitStatus(false, ROUTE_FLIPPER_INDEX);

                    if (mWhateverIsReadyListener != null) {
                        mWhateverIsReadyListener.okImDone();
                    }
                } else {
                    if (!MapsIndoorsUtils.isNetworkReachable(getContext())) {
                        noInternetBar.setState(NoInternetBar.MESSAGE_STATE);
                        noInternetBar.setVisibility(View.VISIBLE);
                    }

                    if (mOrigin != null && mDestination != null) {
                        final Bundle eventParams = new Bundle();
                        eventParams.putString("Origin", mOrigin.getLocationName(mActivity));
                        eventParams.putString("Destination", mDestination.getLocationName(mActivity));
                        eventParams.putLong("Distance", -1);

                        GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_route_calculated), eventParams);
                    }

                    changeWaitStatus(false, NO_ROUTE_FLIPPER_INDEX);
                }
            });
        }
    };

    /**
     *
     * @param locationCallback
     */
    public void getMyPositionRoutingEndpoint(@NonNull GenericObjectResultCallback<RoutingEndPoint> locationCallback) {
        mActivity.getNearestLocationToTheUser((locations, error) -> {

            if (error == null) {
                if ((locations != null) && (locations.size() > 0)) {
                    MPLocation nearestLoc = locations.get(0);
                    RoutingEndPoint routingEndPoint = new RoutingEndPoint(nearestLoc, null, RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING);
                    locationCallback.onResultReady(routingEndPoint);
                } else {
                    MapsIndoorsHelper.getGooglePlacesAddressByPosition(getContext(), mActivity.getCurrentUserPos(), text -> {
                        Building building = MapsIndoors.getBuildings().getBuilding(mActivity.getCurrentUserPos().getLatLng());
                        final MPLocation resLocation;
                        if (building != null) {
                            resLocation = new MPLocation.Builder("UserLocation")
                                    .setPosition(mActivity.getCurrentUserPos())
                                    .setFloor(mActivity.getCurrentUserPos().getZIndex())
                                    .setFloorName(building.getFloorByZIndex(mActivity.getCurrentUserPos().getZIndex()).getDisplayName())
                                    .setBuilding(building.getAdministrativeId())
                                    .setName(getString(R.string.my_position))
                                    .build();
                        }else {
                            resLocation = new MPLocation.Builder("UserLocation")
                                    .setPosition(mActivity.getCurrentUserPos())
                                    .setName(getString(R.string.my_position))
                                    .build();
                        }

                        final String desc = (text != null) ? text : "";

                        RoutingEndPoint routingEndPoint = new RoutingEndPoint(resLocation, desc, RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_OUTSIDE_BUILDING);
                        locationCallback.onResultReady(routingEndPoint);

                    });
                }
            } else {
                if (error.code == MIError.INVALID_API_KEY) {
                    MapsIndoorsUtils.showInvalidAPIKeyDialogue(getContext());
                }
            }
        });
    }

    //region BUTTON CLICK EVENT LISTENERS
    final View.OnClickListener swapButtonClickListener = v -> {
        swapLocations();
        updateList();
    };

    final View.OnClickListener fromButtonOnClickListener = view -> {

        GoogleAnalyticsManager.reportScreen(getString(R.string.ga_screen_select_origin), mActivity);

        mSearchFragment.setSearchType(SearchFragment.DIRECTION_SEARCH_TYPE, SearchFragment.DIRECTION_ORIGIN_SEARCH);

        {
            final String searchBoxString = mSearchFragment.getLastSearchText();
            if (TextUtils.isEmpty(searchBoxString)) {
                final RoutingEndPoint o = getOrigin();
                if (o != null) {
                    final MPLocation location = o.getLocation();
                    if (location != null) {
                        final int type = o.getType();
                        if ((type != RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING) &&
                                (type != RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_OUTSIDE_BUILDING)) {
                            mSearchFragment.setLastSearchText(location.getName());
                        }
                    }
                }
            }
        }

        mSearchFragment.setActive(true);
        mSearchFragment.setOnLocationFoundHandler((queryString, searchResult) -> {
            if (searchResult != null) {
                setOrigin((RoutingEndPoint) searchResult);
                updateList();
                mSearchFragment.setActive(false);
            }
        });
    };

    final View.OnClickListener toButtonOnClickListener = view -> {

        GoogleAnalyticsManager.reportScreen(getString(R.string.ga_screen_select_destination), mActivity);

        mSearchFragment.setSearchType(SearchFragment.DIRECTION_SEARCH_TYPE, SearchFragment.DIRECTION_DESTINATION_SEARCH);

        {
            final String searchBoxString = mSearchFragment.getLastSearchText();
            if (TextUtils.isEmpty(searchBoxString)) {
                final RoutingEndPoint d = getDestination();
                if (d != null) {
                    final MPLocation location = d.getLocation();
                    if (location != null){
                        final int type = d.getType();
                        if ((type != RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING) &&
                                (type != RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_OUTSIDE_BUILDING)) {
                            mSearchFragment.setLastSearchText(location.getName());
                        }
                    }
                }
            }
        }

        mSearchFragment.setActive(true);
        mSearchFragment.setOnLocationFoundHandler((queryString, searchResult) -> {
            if (searchResult != null) {
                setDestination((RoutingEndPoint) searchResult);
                updateList();
                mSearchFragment.setActive(false);
            }
        });
    };
    //endregion
}