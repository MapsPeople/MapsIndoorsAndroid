package com.mapsindoors.stdapp.ui.direction;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
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
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.Floor;
import com.mapsindoors.mapssdk.Highway;
import com.mapsindoors.mapssdk.LineInfo;

import com.mapsindoors.mapssdk.MPFilter;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPQuery;
import com.mapsindoors.mapssdk.MPRoutingProvider;
import com.mapsindoors.mapssdk.Maneuver;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnLocationsReadyListener;
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
import com.mapsindoors.mapssdk.TransitDetails;
import com.mapsindoors.mapssdk.TravelMode;

import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsRouteHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.helpers.SharedPrefsHelper;
import com.mapsindoors.stdapp.listeners.GenericObjectResultCallback;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.managers.UserRolesManager;
import com.mapsindoors.stdapp.models.LastRouteInfo;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.enums.DrawerState;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.common.enums.MenuFrame;
import com.mapsindoors.stdapp.ui.common.listeners.RouteTrackingAdapter;
import com.mapsindoors.stdapp.ui.components.noInternetBar.NoInternetBar;
import com.mapsindoors.stdapp.ui.direction.models.RoutingEndPoint;
import com.mapsindoors.stdapp.ui.search.SearchFragment;
import com.mapsindoors.stdapp.ui.transportagencies.TransportAgenciesFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DirectionsVerticalFragment extends BaseFragment {
    static final String TAG = DirectionsVerticalFragment.class.getSimpleName();


    private static final int ROUTE_FLIPPER_INDEX = 0;
    private static final int NO_ROUTE_FLIPPER_INDEX = 1;
    private static final int LOADING_FLIPPER_INDEX = 2;

    private Context mContext;
    private MapsIndoorsActivity mActivity;

    private MapControl mMapControl;
    private boolean isWaiting = false;

    // Rendering object used to draw routes on top of the google map.
    private Route mCurrentRoute;
    boolean mIsNewRoute;

    private SearchFragment mSearchFragment;
    private final int[] mActionFileId = {R.drawable.ic_vec_sig_lift, R.drawable.ic_vec_sig_escalator, R.drawable.ic_vec_sig_stairs, R.drawable.ic_vec_sig_stairs};
    private final int[] mActionToPrefix = {R.string.prefix_lift, R.string.prefix_escalator, R.string.prefix_stairs, R.string.prefix_escalator};

    private RoutingEndPoint mOrigin;
    private RoutingEndPoint mDestination;

    /**
     * On a new route, the travel mode selected
     */
    @MapsIndoorsHelper.Vehicle
    int mSelectedTravelMode;

    //private SpeakHelper speakHelper;

    private boolean mIsLegIndoors, mIsStarted, mDidExit;

    private ArrayList<RouteStep> mTempRouteStepsList;

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
    private List<AgencyInfo> mTransportAgencies;
    private ViewFlipper mMainViewFlipper;


    // DEBUG
    //Button mDBG_SwitchOnlineOfflineRouting;

    private DirectionsHorizontalFragment directionsHorizontalFragment;

    private NoInternetBar noInternetBar;

    private boolean isStepSelected = false;

    //region Fragment lifecycle events
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.fragment_directions_vertical, container);
        }
        return mMainView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupView(view);
    }

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


    @SuppressLint("SetTextI18n")
    private void setupView(View view) {

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


    interface WhateverIsReadyListener {
        void okImDone();
    }


    public void init(final Context context, final MapControl mapControl) {
        mContext = context;
        mActivity = (MapsIndoorsActivity) context;
        mMapControl = mapControl;

        MapsIndoorsHelper.init(mActivity);
        // don't reset because another call for the updateList will be done in the open function
        resetUI(false);

        initVehicleSelector();

        mBackButton.setOnClickListener((view) -> close());

        disableShowOnMapButton();

        mShowOnMapButton.setOnClickListener(view -> showRouteOnMap(0));

        //
        final boolean avoidStairsSwitchSet = SharedPrefsHelper.getAvoidStairs(mContext);
        mAvoidSwitchView.setChecked(avoidStairsSwitchSet);

        //
        mAvoidSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPrefsHelper.setAvoidStairs(mContext, b);
            updateList();

            // Analytics
            final Bundle eventParams = new Bundle();
            eventParams.putString(getString(R.string.fir_param_Avoid_stairs), mAvoidSwitchView.isChecked() ? "true" : "false");
            GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Avoid_Stairs_Clicked), eventParams);

        });

        mSrcDstFlipButton.setOnClickListener(swapButtonClickListener);

        //Set up a location search object so the user can search for locations
        mSearchFragment = mActivity.getDirectionsFullMenuSearchFragment();
        mSearchFragment.init(context, mapControl);

        mFromTextView.setOnClickListener(fromButtonOnClickListener);
        mToTextView.setOnClickListener(toButtonOnClickListener);

        initVerticalDirectionsPanel();

        //
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


    private void resetVars() {
        mCurrentRoute = null;
        mIsNewRoute = true;
        isStepSelected = false;
    }

    private void initVerticalDirectionsPanel() {
        if (mTempRouteStepsList == null) {
            mTempRouteStepsList = new ArrayList<>();
        } else {
            mTempRouteStepsList.clear();
        }
        if (mTransportAgencies == null) {
            mTransportAgencies = new ArrayList<>();
        } else {
            mTransportAgencies.clear();
        }
        // mIsStarted = true;
    }

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

    void renderRouteOnMap(Route route, int legIndexToSelect) {

        mActivity.setToolbarTitle(getContext().getResources().getString(R.string.toolbar_label_directions), true);
        //First close the routing from the full menu. The small one will show the route now.
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

            //Close the side menu with a small delay

            new Handler(mContext.getMainLooper()).postDelayed(() -> ((MapsIndoorsActivity) mContext).closeDrawer(), 250);
        } else {
            // Just close the menu now, no need to wait
            mActivity.closeDrawer();
            directionsHorizontalFragment.routeNavigateToIndex(legIndexToSelect, true);
        }
    }

    /**
     * Clears the route list, calculate a new route and show that.
     */
    void updateList() {
        // this 2 lines prevent the mScrollView to stay at the same size than it was in the last route
        mRouteLayout.getLayoutParams().height = 0;
        mRouteLayout.requestLayout();

        if ((getOrigin() == null) || (getDestination() == null)) {
            return;
        }

        // in this state of the app it's only when the position of the user changed it will get updated
        refreshEndpoints(() -> {
            //
            new Handler(mContext.getMainLooper()).post(() -> {
                resetDirectionViewLegs();

                // Update the text views (to and from)
                updateTextViews();

                changeWaitStatus(true, ROUTE_FLIPPER_INDEX);

                final String[] avoid = avoidStairs() ? new String[]{Highway.STEPS} : new String[]{};

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

    interface TaskDoneListener {
        void done();
    }

    private void refreshRoutingEndPoint(RoutingEndPoint currentRouteEndpoint, GenericObjectResultCallback<RoutingEndPoint> resultCallback) {
        if (currentRouteEndpoint.getLocation() == null || currentRouteEndpoint.getType() == RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING || currentRouteEndpoint.getType() == RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_OUTSIDE_BUILDING) {
            // if it's the user position routeEndpoint
            getMyPositionRoutingEndpoint(resultCallback);
        } else {
            // otherwise return the current RoutingEndpoint
            resultCallback.onResultReady(currentRouteEndpoint);
        }
    }

    void updateList(@Nullable final Route route) {
        if (mActivity == null) {
            return;
        }

        mActivity.runOnUiThread(() -> {

            if ((getOrigin() == null) || (getDestination() == null)) {
                return;
            }

            resetDirectionViewLegs();

            // Update the text views (to and from)
            updateTextViews();

            changeWaitStatus(true, ROUTE_FLIPPER_INDEX);

            clearCurrentRoute();

            // Assume that we've got a new route when in here...
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

    void swapLocations() {

        RoutingEndPoint swap = getOrigin();

        setOrigin(getDestination());

        setDestination(swap);

        mSearchFragment.swapDirectionSearchData();

        updateTextViews();
        updateRouteEndpointsFromSwap(mOrigin, mDestination);
    }

    void setOrigin(RoutingEndPoint newLocation) {
        mOrigin = newLocation;
    }

    void setDestination(RoutingEndPoint newLocation) {
        mDestination = newLocation;
    }

    @Nullable
    final RoutingEndPoint getOrigin() {
        return mOrigin;
    }

    @Nullable
    final RoutingEndPoint getDestination() {
        return mDestination;
    }

    void setAvoidStairs(boolean avoidStairs) {
        mAvoidSwitchView.setChecked(avoidStairs);
    }

    boolean avoidStairs() {
        return mAvoidSwitchView.isChecked();
    }

    /**
     * Called upon clicking on a google autocomplete result
     *
     * @param location
     * @param isOrigin
     */
    public void OnLocationFoundAction(RoutingEndPoint location, boolean isOrigin) {
        if (isOrigin) {
            setOrigin(location);
        } else {
            setDestination(location);
        }

        updateList();
    }

    public void open(@NonNull MPLocation destination) {
        open(destination, null, null, null);
    }

    public void open(@NonNull MPLocation destination, @Nullable MPLocation origin, @Nullable String travelMode, @Nullable String avoid) {
        mActivity.setOpenLocationMenuFromInfowindowClick(false);

        resetUI(false);
        MapsIndoorsActivity activity = (MapsIndoorsActivity) mContext;

        LastRouteInfo lastRouteInfo = activity.getLastRouteInfo();
        Route prevCalcRoute = activity.getLastRoute();

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

        // to show the text details of the destination
        updateTextViews();
        updateVehicleSelector();

        final boolean showPreCalcRoute = getResources().getBoolean(R.bool.default_starting_position_enabled);
        final boolean hasPrevCalcRoute = prevCalcRoute != null;

        if (showPreCalcRoute && hasPrevCalcRoute) {
            MPLocation prevCalcRouteOriginLocation;
            prevCalcRouteOriginLocation = activity.getLastRouteOrigin();

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
        activity.menuGoTo(MenuFrame.MENU_FRAME_DIRECTIONS_FULL_MENU, true);
    }

    public void close() {
        mActivity.setOpenLocationMenuFromInfowindowClick(true);

        closeRouting();

        mActivity.resetMapToInitialState();

        // go to the previous fragment
        mActivity.menuGoBack();

        noInternetBar.setVisibility(View.GONE);
    }


    WhateverIsReadyListener mWhateverIsReadyListener;
    RoutingProvider routingProvider;

    /**
     * @param origin
     * @param destination
     * @param travelMode
     * @param avoids
     * @param departure    Set to zero to use the current time, or to a epoch time (in seconds)
     * @param arrival      Set to zero to use the current time, or to a epoch time (in seconds). If both departure and arrival are set, arrival is used
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

        // Get a fresh routingProvider for each route request, and make sure we only get callbacks from the latest route request:
        if (routingProvider != null) {
            routingProvider.setOnRouteResultListener(null);
        }
        routingProvider = new MPRoutingProvider();

        // Localize the textual content of the directions service
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

        // Set route restrictions based on the current user's available/preferred user roles
        {
            final List<UserRole> savedUserRoles;
            final UserRolesManager userRolesManager = mActivity.getUserRolesManager();
            if (userRolesManager != null) {
                savedUserRoles = userRolesManager.getSavedUserRoles();
            } else {
                savedUserRoles = null;
            }

            // IMPORTANT: ALWAYS SET THE USER ROLES / CLEAR PREVIOUS SET
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

        // Clear associated search data
        if (mSearchFragment != null) {
            mSearchFragment.clearDirectionSearchData();
        }
    }

    void clearCurrentRoute() {
        if (directionsHorizontalFragment != null) {

            directionsHorizontalFragment.clearRoute();
        }
    }

    private void resetUI(boolean doUpdateList) {
        mOrigin = null;
        mDestination = null;
        mFromTextView.setText("");
        mToTextView.setText("");
        mRouteTitleTextView.setText("");

        if (doUpdateList) {
            updateList();
        }

        initVerticalDirectionsPanel();
        disableShowOnMapButton();
    }

    /**
     * Populate the directions view with the legs from a route
     *
     * @param route
     */
    void render(@Nullable Route route) {
        resetDirectionViewLegs();

        List<RouteLeg> legs = route.getLegs();

        // Check if the first leg is an indoors one
        mIsLegIndoors = MapsIndoorsHelper.isStepInsideBuilding(legs.get(0).getSteps().get(0));

        // distance, duration of route
        float routeDistance, routeDuration;
        routeDistance = routeDuration = 0.0f;
        mTransportAgencies.clear();

        int i = 0;
        int firstStepTravelMode = legs.get(0).getSteps().get(0).getTravelModeVehicle();

        boolean sameTravelMode = true;

        for (RouteLeg currentRouteLeg : legs) {

            // Collect travel distance & duration of the whole route
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


        // Add destination pin
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View foregroundItem = inflater.inflate(R.layout.control_directions_fullmenu_item, null, true);
        mRouteLayout.addView(foregroundItem);

        ImageView circleImageView = foregroundItem.findViewById(R.id.circleImageView);
        TextView prefixTextView = foregroundItem.findViewById(R.id.prefixTextView);
        TextView titleTextView = foregroundItem.findViewById(R.id.titleTextView);
        RelativeLayout itemRelativeLayout = foregroundItem.findViewById(R.id.dir_vert_itemRelativeLayout);
        prefixTextView.setText(mContext.getString(R.string.prefix_end));

        AppConfigManager acm = mActivity.getAppConfigManager();

        circleImageView.setBackgroundResource(R.drawable.ic_direction_end_marker);

        titleTextView.setText(formatPOIText(mDestination, false));


        itemRelativeLayout.setVisibility(View.GONE);

        renderRouteOnMap(route, 0);


        // Add the agencies, if any, if the travel mode is set to TRANSIT
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

                // Set the data
                TransportAgenciesFragment transpAgenciesFrag = mActivity.getTransportAgenciesFragment();
                if (transpAgenciesFrag != null) {
                    transpAgenciesFrag.setList(mTransportAgencies);
                }

                mShowTransportAgenciesButton.setOnClickListener(view -> {
                    // Present the view
                    ((MapsIndoorsActivity) mContext).menuGoTo(MenuFrame.MENU_FRAME_TRANSPORT_AGENCIES, true);
                });
            }
        }

        changeRouteLayoutState(ROUTE_FLIPPER_INDEX);

        mActivity.onRouteRendered();
    }

    private void addLeg(RouteLeg daLeg, final int legIndex) {

        // "Fix" the leg first
        RouteLeg currentRouteLeg = MapsIndoorsHelper.embedOutsideOnVenueSteps(daLeg, mSelectedTravelMode);

        if (currentRouteLeg == null) {
            currentRouteLeg = daLeg;
        }

        List<RouteStep> stepList = currentRouteLeg.getSteps();
        RouteStep firstStep = stepList.get(0);
        int legStepCount = stepList.size();

        if (MapsIndoorsHelper.optimizeOutsideOnVenueSteps(stepList)) {
            // Update the step list count if the optimizer made changes
            legStepCount = stepList.size();
        }

        final int startLevel = currentRouteLeg.getStartPoint().getZIndex();
        int endLevel = currentRouteLeg.getEndPoint().getZIndex();
        boolean isAction = (startLevel != endLevel);

        // Get the current leg context
        boolean isCurrentLegIndoors = MapsIndoorsHelper.isStepInsideBuilding(firstStep);

        // Check if the context has changed from the last leg
        boolean didContextChange = (mIsLegIndoors != isCurrentLegIndoors);

        // Check if the change is from indoors to ...
        mDidExit = didContextChange && !isCurrentLegIndoors;
        mIsLegIndoors = isCurrentLegIndoors;

        mIsStarted = legIndex == 0;

        LayoutInflater inflater = LayoutInflater.from(mContext);


        // in case it's the transit mode, outside steps should be presented as legs
        if (!isCurrentLegIndoors && mSelectedTravelMode == TravelMode.VEHICLE_TRANSIT) {
            mTempRouteStepsList.clear();

            RouteStep currentTravelStep = firstStep;
            RouteStep previousTravelStep = currentTravelStep;
            int nextTravelStepTM;

            for (int i = 0; i < legStepCount; i++) {
                boolean isLastStepInLeg = (i == (legStepCount - 1));
                RouteStep nextStep = ((i + 1) < legStepCount) ? stepList.get(i + 1) : null;
                nextTravelStepTM = (nextStep != null) ? nextStep.getTravelModeVehicle() : -1;

                int currentStepTravelMode = currentTravelStep.getTravelModeVehicle();

                RouteStep step = stepList.get(i);
                int stepTravelMode = step.getTravelModeVehicle();

                if (stepTravelMode == currentStepTravelMode) {
                    mTempRouteStepsList.add(step);
                }

                if (stepTravelMode == TravelMode.VEHICLE_TRANSIT
                        || isLastStepInLeg
                        || nextTravelStepTM != currentStepTravelMode) {
                    addTransitOutSideSteps(previousTravelStep, new ArrayList<>(mTempRouteStepsList), inflater, mDidExit);
                    // lower the flag if it was set
                    mDidExit = false;

                    previousTravelStep = currentTravelStep;
                    mTempRouteStepsList.clear();

                    if (!isLastStepInLeg) {
                        currentTravelStep = nextStep;
                    }
                }
            }

            return;
        }

        LinearLayout foregroundItem = (LinearLayout) inflater.inflate(R.layout.control_directions_fullmenu_item, null, true);
        RelativeLayout mainLayout = foregroundItem.findViewById(R.id.dir_vert_itemRelativeLayout);
        mRouteLayout.addView(foregroundItem);

        final int bgItemIndex = mRouteLayout.getChildCount() - 1;

        mainLayout.setOnClickListener(view -> {

            selectRouteStep(bgItemIndex);
            routeNavigateToIndex(bgItemIndex, true);

        });


        // get all subviews
        ImageView circleImageView = foregroundItem.findViewById(R.id.circleImageView);
        TextView prefixTextView = foregroundItem.findViewById(R.id.prefixTextView);
        TextView titleTextView = foregroundItem.findViewById(R.id.titleTextView);
        View inlineView = foregroundItem.findViewById(R.id.walk_inside_line);

        TextView travelModeTextView = foregroundItem.findViewById(R.id.travelModeTextView);
        TextView distanceTextView = foregroundItem.findViewById(R.id.distanceTextView);


        travelModeTextView.setText(MapsIndoorsHelper.getTravelModeName(firstStep.getTravelModeVehicle()));

        // Add the travel mode icon to all the steps...
        ImageView travelModeImageView = foregroundItem.findViewById(R.id.travelModeImageView);
        if (travelModeImageView != null) {
            int travelModeiconRes = MapsIndoorsHelper.getTravelModeIcon(firstStep.getTravelModeVehicle());
            travelModeImageView.setImageResource(travelModeiconRes);
            travelModeImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.dir_panel_travelmode_icon_tint));
        }

        final ImageView directionArrowImageView = foregroundItem.findViewById(R.id.directionArrowImageView);
        final LinearLayout directionsLinearLayout = foregroundItem.findViewById(R.id.directionsLinearLayout);
        final LinearLayout directionTitleLinearLayout = foregroundItem.findViewById(R.id.directionTitleLinearLayout);

        directionTitleLinearLayout.setOnClickListener(view -> {

            if (directionsLinearLayout.getVisibility() == View.GONE) {
                directionsLinearLayout.setVisibility(View.VISIBLE);
                directionArrowImageView.setImageResource(R.drawable.ic_expand_less);

                //region REPORT TO ANALYTICS
                GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_directions_expanded), null);
                //endregion

            } else {
                directionsLinearLayout.setVisibility(View.GONE);
                directionArrowImageView.setImageResource(R.drawable.ic_expand_more);
            }
        });

        distanceTextView.setText(String.format("%s (%s)",
                MapsIndoorsRouteHelper.getFormattedDistance((int) currentRouteLeg.getDistance()),
                MapsIndoorsRouteHelper.getFormattedDuration((int) currentRouteLeg.getDuration())));

        // if mIsLegIndoors is "InsideBuilding", directions is hidden
        inlineView.setVisibility(View.VISIBLE);


        // start point
        if (mIsStarted) {
            //  mIsStarted = false;

            prefixTextView.setText(mContext.getString(R.string.prefix_start));

            titleTextView.setText(formatPOIText(mOrigin, false));

        }

        // Determine enter or exit
        if (didContextChange) {

            circleImageView.setImageResource(isCurrentLegIndoors ? R.drawable.ic_vec_sig_enter : R.drawable.ic_vec_sig_exit);
            prefixTextView.setText(isCurrentLegIndoors ? getString(R.string.prefix_enter) : getString(R.string.prefix_exit));

            final BuildingCollection bc = MapsIndoors.getBuildings();
            Building building = bc.getBuilding(firstStep.getStartGLatLng());
            String buildingName = (building == null) ? "Outside" : building.getName();
            titleTextView.setText(buildingName);
        } else if (!mIsStarted) {
            circleImageView.setImageResource(R.drawable.ic_local_parking_black_24dp);
            titleTextView.setText(firstStep.getStartLocation().label);

            prefixTextView.setText(mContext.getString(R.string.prefix_park));


        }

        // determine whether it is action point for not
        if (isAction) {

            // set default leg title
            titleTextView.setText(getStepName(firstStep, stepList.get(legStepCount - 1)));

            String[] actionNames = MapsIndoorsHelper.getActionNames();
            String firstStepHighWay = firstStep.getHighway();

            for (int idx = actionNames.length; --idx >= 0; ) {
                if (firstStepHighWay.equalsIgnoreCase(actionNames[idx])) {
                    circleImageView.setImageResource(mActionFileId[idx]);
                    prefixTextView.setText(mContext.getString(mActionToPrefix[idx]));
                    break;
                }
            }
        } else if (!mIsStarted && !didContextChange) {
            MPQuery query = new MPQuery.Builder()
                    .setQuery("Parking")
                    .setNear(firstStep.getStartPoint())
                    .build();

            ArrayList<String> categories = new ArrayList<>();
            categories.add("Parking");
            MPFilter filter = new MPFilter.Builder().setFloorIndex(0).setCategories(categories).build();
            MapsIndoors.getLocationsAsync(query, filter, new OnLocationsReadyListener() {
                @Override
                @UiThread
                public void onLocationsReady(@Nullable List<MPLocation> list, @Nullable MIError miError) {
                    if (mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_BICYCLING) {
                        for (MPLocation mpLocation : list) {
                            if (mpLocation.getType().toLowerCase().contains("bike")) {
                                titleTextView.setText(mpLocation.getName());
                                break;
                            }
                        }
                    } else {
                        for (MPLocation mpLocation : list) {
                            if (!mpLocation.getType().toLowerCase().contains("bike")) {
                                titleTextView.setText(mpLocation.getName());
                                break;
                            }
                        }
                    }
                    if (titleTextView.getText().length() == 0) {
                        titleTextView.setText(list.get(0).getName());
                    }
                }
            });
        }
    }

    private void addTransitOutSideSteps(RouteStep previousTravelStep, List<RouteStep> stepList, LayoutInflater inflater, boolean didExitVenue) {
        boolean travelModeIsTransit = mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_TRANSIT;

        LinearLayout foregroundItem = (LinearLayout) inflater.inflate(R.layout.control_directions_fullmenu_item, null, true);
        RelativeLayout mainLayout = foregroundItem.findViewById(R.id.dir_vert_itemRelativeLayout);

        mRouteLayout.addView(foregroundItem);

        final int bgItemIndex = mRouteLayout.getChildCount() - 1;

        mainLayout.setOnClickListener(v -> routeNavigateToIndex(bgItemIndex, true));


        // get all subviews
        ImageView circleImageView = foregroundItem.findViewById(R.id.circleImageView);
        ImageView travelActionImageView = foregroundItem.findViewById(R.id.travelActionImageView);
        ImageView travelAction1ImageView = foregroundItem.findViewById(R.id.travelAction1ImageView);
        ImageView travelCircleImageView = foregroundItem.findViewById(R.id.travelCircleImageView);

        TextView prefixTextView = foregroundItem.findViewById(R.id.prefixTextView);
        TextView titleTextView = foregroundItem.findViewById(R.id.titleTextView);
        //View inlineView		   = forgroundItem.findViewById(R.id.inlineView);
        View buslineView = foregroundItem.findViewById(R.id.bus_line);

        TextView travelModeTextView = foregroundItem.findViewById(R.id.travelModeTextView);
        ImageView travelModeImageView = foregroundItem.findViewById(R.id.travelModeImageView);
        TextView distanceTextView = foregroundItem.findViewById(R.id.distanceTextView);

        TextView stopsTextView = foregroundItem.findViewById(R.id.stopsTextView);

        final ImageView directionArrowImageView = foregroundItem.findViewById(R.id.directionArrowImageView);
        final LinearLayout directionsLinearLayout = foregroundItem.findViewById(R.id.directionsLinearLayout);
        final LinearLayout directionTitleLinearLayout = foregroundItem.findViewById(R.id.directionTitleLinearLayout);


        directionTitleLinearLayout.setOnClickListener(view -> {
            if (directionsLinearLayout.getVisibility() == View.GONE) {
                directionsLinearLayout.setVisibility(View.VISIBLE);
                directionArrowImageView.setImageResource(R.drawable.ic_expand_less);
            } else {
                directionsLinearLayout.setVisibility(View.GONE);
                directionArrowImageView.setImageResource(R.drawable.ic_expand_more);
            }
        });

        RouteStep firstStep = stepList.get(0);
        final BuildingCollection bc = MapsIndoors.getBuildings();
        Building building = bc.getBuilding(firstStep.getStartGLatLng());
        int firstStepTravelMode = firstStep.getTravelModeVehicle();

        if (firstStepTravelMode != TravelMode.VEHICLE_BICYCLING
                && firstStepTravelMode != TravelMode.VEHICLE_DRIVING) {
            final View walkOutsideLineView = foregroundItem.findViewById(R.id.walk_outside_line);
            walkOutsideLineView.setVisibility(View.VISIBLE);
        } else {
            final View driveBikeLineView = foregroundItem.findViewById(R.id.drive_bike_outside_line);
            driveBikeLineView.setVisibility(View.VISIBLE);
        }

        directionTitleLinearLayout.setVisibility(View.VISIBLE);

        float distance, duration;
        distance = duration = 0f;

        //travelModeIsTransit
        final List<RouteStep> directionsList = !travelModeIsTransit ? stepList : firstStep.getSteps();

        if (directionsList != null) {

            for (int i = 0, dirStepCount = directionsList.size(); i < dirStepCount; i++) {
                RouteStep step = directionsList.get(i);
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

                    // Modify the maneuver if this highway is of type STEPS
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

        // if enter or exit
        if (didExitVenue) {
            circleImageView.setImageResource(R.drawable.ic_vec_sig_exit);
            prefixTextView.setText(getString(R.string.prefix_exit));

            String buildingName = (building == null) ? "Outside" : building.getName();
            titleTextView.setText(buildingName);
        }

        travelModeTextView.setText(MapsIndoorsHelper.getTravelModeName(firstStepTravelMode));

        //

        if (travelModeImageView != null) {
            int travelModeiconRes = MapsIndoorsHelper.getTravelModeIcon(firstStepTravelMode);
            travelModeImageView.setImageResource(travelModeiconRes);
            travelModeImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.dir_panel_travelmode_icon_tint));
        }

        //
        TransitDetails transitDetails = firstStep.getTransitDetails();
        TransitDetails previousTransitDetails = previousTravelStep.getTransitDetails();

        if (transitDetails != null && firstStepTravelMode == TravelMode.VEHICLE_TRANSIT) {
            buslineView.setVisibility(View.VISIBLE);
            travelAction1ImageView.setVisibility(View.VISIBLE);
            travelCircleImageView.setVisibility(View.VISIBLE);

            circleImageView.setVisibility(View.GONE);
            travelAction1ImageView.setImageResource(R.drawable.ic_bus_up);
            travelActionImageView.setScaleType(ImageView.ScaleType.FIT_END);

            directionTitleLinearLayout.setVisibility(View.GONE);
            stopsTextView.setVisibility(View.VISIBLE);

            LineInfo transitDetailsLineInfo = transitDetails.getLine();
            if (transitDetailsLineInfo != null) {

                String lineName = transitDetailsLineInfo.getShort_name();
                if (lineName == null) {
                    lineName = transitDetailsLineInfo.getName();
                }

                if (transitDetailsLineInfo.getAgencies() != null) {
                    mTransportAgencies.addAll(transitDetailsLineInfo.getAgencies());
                }

                travelModeTextView.setText(lineName);

                // In case of not having a text color, set it to black
                travelModeTextView.setTextColor(MapsIndoorsRouteHelper.getTransitDetailsLineTextColor(mContext, transitDetails));

                // In case of not having a bg/line color, set it to grey
                int tdLineColor = MapsIndoorsRouteHelper.getTransitDetailsLineColor(mContext, transitDetails);
                travelModeTextView.setBackgroundColor(tdLineColor);

                buslineView.setBackgroundColor(tdLineColor);
                travelAction1ImageView.setColorFilter(tdLineColor);

                // Get the default vehicle icon
                String vehicleIconURL = transitDetailsLineInfo.getVehicle().getLocal_icon();

                // If the local version is not present, use gmap's default
                if (vehicleIconURL == null) {
                    vehicleIconURL = transitDetailsLineInfo.getVehicle().getIcon();
                }

                travelModeImageView.clearColorFilter();

                Picasso.get()
                        .load("http:" + vehicleIconURL)
                        .into(travelModeImageView);
            }

            distanceTextView.setText(transitDetails.getHeadsign());

            if (transitDetails.getDeparture_stop() != null) {
                titleTextView.setText(transitDetails.getDeparture_stop().getName());
            }

            String stopStr = (transitDetails.getNum_stops() >= 1) ? getResources().getString(R.string.stops) : getResources().getString(R.string.stop);
            duration = firstStep.getDuration();

            stopsTextView.setText(String.format(Locale.US, "%d %s (%s)", transitDetails.getNum_stops(), stopStr, MapsIndoorsRouteHelper.getFormattedDuration((int) duration)));
        }

        if (previousTransitDetails != null && previousTravelStep.getTravelModeVehicle() == TravelMode.VEHICLE_TRANSIT) {
            circleImageView.setVisibility(View.GONE);
            travelActionImageView.setVisibility(View.VISIBLE);
            travelActionImageView.setImageResource(R.drawable.ic_bus_down);
            travelActionImageView.setScaleType(ImageView.ScaleType.FIT_START);


            if (previousTransitDetails.getArrival_stop() != null) {
                titleTextView.setText(previousTransitDetails.getArrival_stop().getName());
            }

            if (previousTransitDetails.getLine() != null) {
                // In case of not having a bg/line color, set it to grey
                travelActionImageView.setColorFilter(MapsIndoorsRouteHelper.getTransitDetailsLineColor(mContext, previousTransitDetails));
            }
        }

        // start point
        if (mIsStarted) {
            //  mIsStarted = false;

            circleImageView.setVisibility(View.VISIBLE);
            travelActionImageView.setVisibility(View.GONE);
            travelAction1ImageView.setVisibility(View.GONE);
            prefixTextView.setText(mContext.getString(R.string.prefix_start));
            titleTextView.setText(formatPOIText(mOrigin, false));

        }
    }

    private String getStepName(RouteStep startStep, RouteStep endStep) {

        String result = startStep.getStartFloorName();

        if ((result == null) || (result.length() == 0)) {
            result = String.format("%s %s", getString(R.string.level), startStep.getStartPoint().getZIndex());
        } else {
            result = String.format("%s %s", getString(R.string.level), result);
        }

        if (startStep.getStartPoint().getZIndex() == endStep.getEndPoint().getZIndex()) {
            return result;
        }else {
            Building startBuilding = MapsIndoors.getBuildings().getBuilding(startStep.getStartGLatLng());
            Building endBuilding = MapsIndoors.getBuildings().getBuilding(endStep.getEndGLatLng());
            if (startBuilding != null && endBuilding != null) {
                Floor startFloor = startBuilding.getFloorByZIndex(startStep.getStartPoint().getZIndex());
                Floor endFloor = endBuilding.getFloorByZIndex(endStep.getEndPoint().getZIndex());
                if (startFloor != null && endFloor != null) {
                    String startFloorName = startFloor.getDisplayName();
                    String endFloorName = endFloor.getDisplayName();

                    result = String.format("%s %s", getString(R.string.level), startFloorName) + " to " + endFloorName;
                    return result;
                }
            }
        }

        String endFloorName = endStep.getEndFloorName();

        if (endFloorName == null || (endFloorName.length() == 0)) {
            result += " to " + endStep.getEndPoint().getZIndex();
        } else {
            result += " to " + endFloorName;
        }

        return result;
    }


    /**
     * Removed all legs from the view leaving it blank.
     */
    public void resetDirectionViewLegs() {
        mRouteLayout.removeAllViewsInLayout();
        mRouteLayout.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    //A waiting spinner will appear is set to true and be removed again on false.
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
    private void initVehicleSelector() {
        mVehicleWalkImageView.setOnClickListener(mVehicleSelectorOnClickListener);
        mVehicleBicycleImageView.setOnClickListener(mVehicleSelectorOnClickListener);
        mVehicleTransitImageView.setOnClickListener(mVehicleSelectorOnClickListener);
        mVehicleCarImageView.setOnClickListener(mVehicleSelectorOnClickListener);
    }

    void updateVehicleSelector() {
        mVehicleWalkImageView.setAlpha(mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_WALKING ? 1.0f : 0.5f);
        mVehicleBicycleImageView.setAlpha(mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_BICYCLING ? 1.0f : 0.5f);
        mVehicleTransitImageView.setAlpha(mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_TRANSIT ? 1.0f : 0.5f);
        mVehicleCarImageView.setAlpha(mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_DRIVING ? 1.0f : 0.5f);

        SharedPrefsHelper.setUserTravelingMode(mContext, mSelectedTravelMode);
    }

    final View.OnClickListener mVehicleSelectorOnClickListener = view -> {

        @MapsIndoorsHelper.Vehicle final int selTravelMode;

        switch (view.getId()) {
            default:
            case R.id.imageViewWalk:
                selTravelMode = MapsIndoorsHelper.VEHICLE_WALKING;
                break;
            case R.id.imageViewBicycle:
                selTravelMode = MapsIndoorsHelper.VEHICLE_BICYCLING;
                break;
            case R.id.imageViewTransit:
                selTravelMode = MapsIndoorsHelper.VEHICLE_TRANSIT;
                break;
            case R.id.imageVehicleCar:
                selTravelMode = MapsIndoorsHelper.VEHICLE_DRIVING;
                break;
        }

        if (selTravelMode != mSelectedTravelMode) {
            mSelectedTravelMode = selTravelMode;
            vehicleSelectorClicked();
        }
    };

    void vehicleSelectorClicked() {
        updateVehicleSelector();
        updateList();
        reportTravelModeToAnalytics();
    }

    void reportTravelModeToAnalytics() {
        final Bundle eventParams = new Bundle();

        eventParams.putString(getString(R.string.fir_param_Travel_Mode), getSelectedTravelMode());

        GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Travel_Mode_Selected), eventParams);
    }

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

    @Override
    public void willOpen(final MenuFrame fromFrame) {
        if (fromFrame == MenuFrame.MENU_FRAME_ROUTE_OPTIONS) {
            // Ideal: when coming from the route options, refresh only if there were any changes made
            updateList();
        }
    }
    //endregion


    //region IMPLEMENTS OnStateChangedListener
    final OnStateChangedListener onPositionProviderStateChangedListener = isEnabled -> updateList();
    //endregion


    void disableShowOnMapButton() {
        mShowOnMapButton.setAlpha(.5f);
        mShowOnMapButton.setEnabled(false);
    }

    void enableShowOnMapButton() {
        mShowOnMapButton.setAlpha(1);
        mShowOnMapButton.setEnabled(true);
    }

    private void prepareTransportAgenciesList(List<AgencyInfo> list) {
        // Sort the agencies
        Collections.sort(list, (a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));

        // Remove repeated agencies
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

    private static String ANALYTICS_VERTICAL_DIRECTION_PANEL_PARAM = "Vertical";


    void routeNavigateToIndex(int index, boolean animate) {
        //region REPORT TO ANALYTICS
        {
            float segmentPositionFactor = index / mCurrentRoute.getLegs().size();

            final Bundle eventParams = new Bundle();

            eventParams.putFloat(getString(R.string.fir_param_Segment_Position_Factor), segmentPositionFactor);
            eventParams.putString(getString(R.string.fir_param_Directions_Layout), ANALYTICS_VERTICAL_DIRECTION_PANEL_PARAM);

            GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Directions_Route_Segment_Selected), eventParams);
        }

        mActivity.getUserPositionTrackingViewModel().stopTracking();

        showRouteOnMap(index);
    }

    void changeRouteLayoutState(int index) {
        mMainViewFlipper.setDisplayedChild(index);
    }

    int getCurrentLayoutState() {
        return mMainViewFlipper.getDisplayedChild();
    }

    String formatPOIText(RoutingEndPoint routingEndPoint, boolean isDestination) {

        String name = routingEndPoint.getLocationName(mActivity);
        String formattedDetails = routingEndPoint.getFormattedDetails(mActivity);

        if (formattedDetails == null || formattedDetails.equals("")) {
            return String.format("%s", name);

        } else {
            if (isDestination) {
                return String.format("%s, %s", name, formattedDetails);
            } else {
                return String.format("%s (%s)", name, formattedDetails);
            }
        }

    }

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


    //region IMPLEMENTS OnRouteResultListener
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


                    //hide the no internet message
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

                    // Report via de listener only positive results
                    if (mWhateverIsReadyListener != null) {
                        mWhateverIsReadyListener.okImDone();
                    }
                } else {
                    //show the internet message if there is no internet connection
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
    //endregion


    public void getMyPositionRoutingEndpoint(@NonNull GenericObjectResultCallback<RoutingEndPoint> locationCallback) {
        mActivity.getNearestLocationToTheUser((locations, error) -> {

            if (error == null) {
                if ((locations != null) && (locations.size() > 0)) {
                    // the user is close to a POI
                    MPLocation nearestLoc = locations.get(0);
                    RoutingEndPoint routingEndPoint = new RoutingEndPoint(nearestLoc, null, RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING);
                    locationCallback.onResultReady(routingEndPoint);
                } else {
                    MapsIndoorsHelper.getGooglePlacesAddressByPosition(getContext(), mActivity.getCurrentUserPos(), text -> {
                        Building building = MapsIndoors.getBuildings().getBuilding(mActivity.getCurrentUserPos().getLatLng());
                        final MPLocation resLocation;
                        if (building != null) {
                            // the user is inside building
                            resLocation = new MPLocation.Builder("UserLocation")
                                    .setPosition(mActivity.getCurrentUserPos())
                                    .setFloor(mActivity.getCurrentUserPos().getZIndex())
                                    .setFloorName(building.getFloorByZIndex(mActivity.getCurrentUserPos().getZIndex()).getDisplayName())
                                    .setBuilding(building.getAdministrativeId())
                                    .setName(getString(R.string.my_position))
                                    .build();
                        }else {
                            // the user is outside building
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
        //swap the location and update the list (with the new route)
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
                //User selected a location.
                setOrigin((RoutingEndPoint) searchResult);
                updateList();
                //mSearchFragment.setLastSearchText( queryString );
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
                    final int type = d.getType();
                    if ((type != RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING) &&
                            (type != RoutingEndPoint.ENDPOINT_TYPE_MY_POSITION_OUTSIDE_BUILDING)) {
                        mSearchFragment.setLastSearchText(d.getLocation().getName());
                    }
                }
            }
        }

        mSearchFragment.setActive(true);
        mSearchFragment.setOnLocationFoundHandler((queryString, searchResult) -> {
            if (searchResult != null) {
                //User selected a location.
                setDestination((RoutingEndPoint) searchResult);
                updateList();
                //mSearchFragment.setLastSearchText( queryString );
                mSearchFragment.setActive(false);
            }
        });
    };
    //endregion
}