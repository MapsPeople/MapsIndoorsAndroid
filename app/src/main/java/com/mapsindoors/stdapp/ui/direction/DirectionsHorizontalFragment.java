package com.mapsindoors.stdapp.ui.direction;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
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
import com.mapsindoors.mapssdk.Building;
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.Floor;
import com.mapsindoors.mapssdk.MPDirectionsRenderer;
import com.mapsindoors.mapssdk.MPFilter;
import com.mapsindoors.mapssdk.MPLocation;

import com.mapsindoors.mapssdk.MPQuery;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnLegSelectedListener;

import com.mapsindoors.mapssdk.OnLocationsReadyListener;
import com.mapsindoors.mapssdk.Route;
import com.mapsindoors.mapssdk.RouteCoordinate;
import com.mapsindoors.mapssdk.RouteLeg;
import com.mapsindoors.mapssdk.RouteStep;
import com.mapsindoors.mapssdk.SphericalUtil;
import com.mapsindoors.mapssdk.TransitDetails;
import com.mapsindoors.mapssdk.TravelMode;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsRouteHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.listeners.IDirectionPanelListener;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.models.UIRouteNavigation;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.direction.listeners.RouteListener;
import com.mapsindoors.stdapp.ui.direction.models.RoutingEndPoint;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Fragment that shows routes on the map (using mDirectionsRenderer)
 */
public class DirectionsHorizontalFragment extends BaseFragment
        implements
        IDirectionPanelListener,
        OnLegSelectedListener {
    static final String TAG = DirectionsHorizontalFragment.class.getSimpleName();


    private Route mCurrentRoute;

    // Rendering object used to draw routes on top of the google map.
    private MPDirectionsRenderer mMyDirectionsRenderer;

    private GoogleMap mGoogleMap;
    private MapsIndoorsActivity mActivity;
    private Context mContext;
    private MapControl mMapControl;

    private int[] mActionFileId = {R.drawable.ic_vec_sig_lift, R.drawable.ic_vec_sig_escalator, R.drawable.ic_vec_sig_stairs, R.drawable.ic_vec_sig_stairs};


    private RoutingEndPoint mOrigin, mDestination;

    /**
     * On a new route, the travel mode selected
     */
    private @MapsIndoorsHelper.Vehicle
    int mSelectedTravelMode;


    // ----------------------------------
    private boolean mIsLegIndoors, mIsStarted, mDidExit;

    private HorizontalScrollView mScrollingContainerLayout;
    private View mPanelToolbar, mPanelControls;
    private LinearLayout mBackgroundLayout, mForegroundLayout;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private ImageButton mCancelBtn;

    private ArrayList<RouteStep> mTempRouteList;

    private int mCurrentRouteNaviIndex;

    /**
     * Navigation object, to be used by the prev/next buttons
     */
    private List<UIRouteNavigation> mUINaviList;

    private List<RouteCoordinate> mTmpRCPointList;

    /**
     * Viewport screen coordinates TL, TR, BL, BR
     */
    private int mMapViewPortScreenWidth, mMapViewPortScreenHeight;
    private android.graphics.Point mViewPortScreenCenter;
    private int mViewPortPadding;

    private RouteListener mRouteListener;


    //region Fragment lifecycle events
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupView(view);
    }
    //endregion


    private void setupView(View view) {
        mPanelToolbar = view.findViewById(R.id.directions_menu_horiz_toolbar);
        mPanelControls = view.findViewById(R.id.directions_fullmenu_horiz_controls);
        mScrollingContainerLayout = view.findViewById(R.id.directions_menu_horiz_horizontalScrollView);
        mBackgroundLayout = view.findViewById(R.id.directions_background);
        mForegroundLayout = view.findViewById(R.id.directions_foreground);
        mNextButton = view.findViewById(R.id.directions_menu_horiz_button_next);
        mPreviousButton = view.findViewById(R.id.directions_menu_horiz_button_prev);
        mCancelBtn = view.findViewById(R.id.directions_menu_horiz_toolbar_button_back);
    }

    public void init(@NonNull final MapsIndoorsActivity activity, @NonNull GoogleMap map) {
        mActivity = activity;
        mContext = activity;
        mMapControl = activity.getMapControl();

        mGoogleMap = map;

        mViewPortScreenCenter = new android.graphics.Point(0, 0);

        MapsIndoorsHelper.init(activity);


        mMyDirectionsRenderer = new MPDirectionsRenderer(mContext, map, mMapControl, this);
        mMyDirectionsRenderer.setAnimated(false);

        mMyDirectionsRenderer.setPrimaryColor(ContextCompat.getColor(mContext, R.color.directionLegColor));
        mMyDirectionsRenderer.setAccentColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        mMyDirectionsRenderer.setTextColor(ContextCompat.getColor(mContext, R.color.white));

        initHorizontalDirectionsPanel();
    }

    private void initHorizontalDirectionsPanel() {
        mTempRouteList = new ArrayList<>();
        mUINaviList = new ArrayList<>();
        mTmpRCPointList = new ArrayList<>();

        mIsLegIndoors = true;

        // Default to walking
        mSelectedTravelMode = MapsIndoorsHelper.VEHICLE_WALKING;

        // Setup the route navigation buttons
        mNextButton.setOnClickListener(v -> routeNavigateToNext());

        mPreviousButton.setOnClickListener(v -> routeNavigateToPrev());

        // Setup the cancel button
        mCancelBtn.setOnClickListener(v -> closeAndOpenMenu());
    }

    void closeAndOpenMenu() {
        setActive(false);
        ((MapsIndoorsActivity) mContext).openDrawer(true);
    }

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

    public void setCurrentTravelMode(@Nullable String travelMode) {
        mSelectedTravelMode = MapsIndoorsHelper.travelModeToVehicle(travelMode);
    }

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
     *
     * @param active
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
                // hide the floor selector...
                mMapControl.enableFloorSelector(false);

                // Calc some the viewport width, height and center, after taking the horizontal panel's height
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

    public void setShow(boolean show) {
        if (isActive() == show) {
            return;
        }

        if (mMainView != null) {
            mMainView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public int getPanelHeight() {
        // Sum up all the three layout heights

        return mPanelToolbar.getHeight() + mScrollingContainerLayout.getHeight() + mPanelControls.getHeight();
    }


    //region Implements IDirectionPanelListener

    /**
     * Highlights the given route leg
     *
     * @param legIndex
     */
    @Override
    public void onLegSelected(final int legIndex, int itemIndex) {
        mMyDirectionsRenderer.setAlpha(255);


        new Handler(mContext.getMainLooper()).postDelayed(() -> {

            // Highlight the given route's leg
            mMyDirectionsRenderer.setRouteLegIndex(legIndex);
            //=============================================================

            //if the next navObj is indoor then switch to the navObj floor


            RouteLeg nextLeg = (legIndex < mCurrentLegs.size() - 1) ? mCurrentLegs.get(legIndex + 1) : null;
            RouteLeg currentLeg = mCurrentLegs.get(legIndex);
            final int legFloor;
            // when the current leg is google maps and the next is mapsindoors then we should select the floor of the next leg otherwise select the floor chosen by the renderer
            if (nextLeg != null && nextLeg.isMapsIndoors() && !currentLeg.isMapsIndoors()) {
                legFloor = nextLeg.getEndPoint().getZIndex();

            } else {
                // Select the floor (highlights it in the UI)
                legFloor = mMyDirectionsRenderer.getLegFloor();
            }


            if (mMapControl.getCurrentFloorIndex() != legFloor) {
                mMapControl.selectFloor(legFloor);

            }

        }, 100);

    }

    @Override
    public void onStepSelected(final int legIndex, final int stepIndex, int itemIndex) {
        mMyDirectionsRenderer.setAlpha(255);

        new Handler(mContext.getMainLooper()).postDelayed(() -> {

            mMyDirectionsRenderer.setRouteLegIndex(legIndex, stepIndex);

            RouteLeg nextLeg = (legIndex < mCurrentLegs.size() - 1) ? mCurrentLegs.get(legIndex + 1) : null;
            RouteLeg currentLeg = mCurrentLegs.get(legIndex);
            final int legFloor;

            // when the current leg is google maps and the next is mapsindoors then we should select the floor of the next leg otherwise select the floor chosen by the renderer
            if (nextLeg != null && nextLeg.isMapsIndoors() && !currentLeg.isMapsIndoors()) {
                legFloor = nextLeg.getEndPoint().getZIndex();

            } else {
                // Select the floor (highlights it in the UI)
                legFloor = mMyDirectionsRenderer.getLegFloor();
            }

            if (mMapControl.getCurrentFloorIndex() != legFloor) {
                mMapControl.selectFloor(legFloor);

            }

        }, 100);
    }

    //endregion


    //region Implements OnLegSelectedListener
    @Override
    public void onLegSelected(int nextLegIndex) {
        if (mCurrentRoute != null) {
            int legCount = mCurrentRoute.getLegs().size();

            if (nextLegIndex < legCount) {
                routeNavigateToIndex(legToUINaviIndex(nextLegIndex), true);
            }
        }
    }
    //endregion

    /**
     * Maps a leg index to a navi segment index
     *
     * @param legIndex
     * @return
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
     *
     * @param route
     */
    void renderDirectionsView(Route route) {
        mMapControl.setMapPadding(0,0,0,getView().getHeight() );
        mActivity.setPositioningBtnBottomPadding(getView().getHeight());

        resetLegs();
        mCurrentLegs = route.getLegs();
        List<RouteLeg> legs = route.getLegs();

        // Check if the first leg is an indoors one
        mIsLegIndoors = MapsIndoorsHelper.isStepInsideBuilding(legs.get(0).getSteps().get(0));

        for (int i = 0, aLen = legs.size(); i < aLen; i++) {
            addLeg(legs.get(i), i);
        }

        // Add mDestination  (last) pin
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


    private void addLeg(RouteLeg currentRouteLeg, int legIndex) {
        // "Fix" the leg first

        List<RouteStep> stepList = currentRouteLeg.getSteps();
        RouteStep firstStep = stepList.get(0);
        int legStepCount = stepList.size();

        int startLevel = currentRouteLeg.getStartPoint().getZIndex();
        int endLevel = currentRouteLeg.getEndPoint().getZIndex();
        boolean isAction = (startLevel != endLevel);

        // Get the current leg context
        boolean isCurrentLegIndoors = MapsIndoorsHelper.isStepInsideBuilding(firstStep);

        // Check if the context has changed from the last leg
        boolean didContextChange = (mIsLegIndoors != isCurrentLegIndoors);

        mIsStarted = legIndex == 0;
        // Raise the exit flag on context change, from Indoors to out
        mDidExit = didContextChange && !isCurrentLegIndoors;
        mIsLegIndoors = isCurrentLegIndoors;

        LayoutInflater inflater = LayoutInflater.from(mContext);

        // in case it's the transit mode, outside steps should be presented as legs
        if (!isCurrentLegIndoors && mSelectedTravelMode == TravelMode.VEHICLE_TRANSIT) {
            mTempRouteList.clear();

            RouteStep currentTravelStep = firstStep;
            RouteStep previousTravelStep = currentTravelStep;
            int nextTravelStepTM;

            for (int i = 0; i < legStepCount; i++) {
                boolean isLastStepInLeg = (i == (legStepCount - 1));
                RouteStep nextTravelStep = ((i + 1) < legStepCount) ? stepList.get(i + 1) : null;
                nextTravelStepTM = nextTravelStep != null ? nextTravelStep.getTravelModeVehicle() : -1;

                int currentStepTravelMode = currentTravelStep.getTravelModeVehicle();

                RouteStep step = stepList.get(i);
                int stepTravelMode = step.getTravelModeVehicle();

                if (stepTravelMode == currentStepTravelMode) {
                    mTempRouteList.add(step);
                }

                if (stepTravelMode == TravelMode.VEHICLE_TRANSIT
                        || isLastStepInLeg
                        || !(nextTravelStepTM == currentStepTravelMode)) {
                    addTransitOutSideSteps(legIndex, i, previousTravelStep, new ArrayList<>(mTempRouteList), inflater, mDidExit);

                    // lower the flag if it was set
                    mDidExit = false;

                    previousTravelStep = currentTravelStep;
                    mTempRouteList.clear();

                    if (!isLastStepInLeg) {
                        currentTravelStep = nextTravelStep;
                    }
                }
            }

            return;
        }

        //
        View backgroundItem = inflater.inflate(R.layout.control_directions_menu_item_bg, null, true);
        View foregroundItem = inflater.inflate(R.layout.control_directions_menu_item_fg, null, true);
        //
        mBackgroundLayout.addView(backgroundItem);
        mForegroundLayout.addView(foregroundItem);

        // Get the index of the current BG item to highlight when clicking on it
        final int bgItemIndex = mBackgroundLayout.getChildCount() - 1;

        //  ADD THE UI NAVIGATION HERE
        final int selItemIndex = mUINaviList.size();

        //
        UIRouteNavigation naviObj = generateNaviListObj(currentRouteLeg, null, mIsLegIndoors);

        if (dbglog.isDeveloperMode()) {
            dbglog.Assert(naviObj != null, "UIRouteNavigation not created, reason: no points found in currentRouteLeg");
        }

        naviObj.set(mIsLegIndoors, legIndex, bgItemIndex);
        mUINaviList.add(naviObj);

        backgroundItem.setOnClickListener(v -> routeItemClickCallback(selItemIndex, true));

        // BG item element refs
        View inlineView = backgroundItem.findViewById(R.id.dir_horiz_walk_inside_line);

        // FG item element refs
        ImageView circleImageView = foregroundItem.findViewById(R.id.dir_horiz_circleImageView);
        TextView titleTextView = foregroundItem.findViewById(R.id.dir_horiz_titleTextView);

        // If abutters is "InsideBuilding", directions is hidden
        inlineView.setVisibility(View.VISIBLE);

        // Add the travel mode icon to all the steps...
        ImageView travelModeImageView = backgroundItem.findViewById(R.id.dir_horiz_travelModeImageView);
        if (travelModeImageView != null) {
            int travelModeiconRes = MapsIndoorsHelper.getTravelModeIcon(firstStep.getTravelModeVehicle());
            travelModeImageView.setImageResource(travelModeiconRes);
            travelModeImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.dir_panel_travelmode_icon_tint));
        }

        // Check if this is a starting point
        if (mIsStarted) {
            String locationLabel = mOrigin.getLocationName(mActivity);
            String desc = mOrigin.getFormattedDetails(mActivity);

            locationLabel = String.format(Locale.US, "%s\n(%s)", locationLabel, desc);
            titleTextView.setText(locationLabel);
        }

        // If enter or exit
        if (didContextChange) {
            circleImageView.setImageResource(isCurrentLegIndoors ? R.drawable.ic_vec_sig_enter : R.drawable.ic_vec_sig_exit);

            final BuildingCollection bc = MapsIndoors.getBuildings();
            Building building = bc.getBuilding(firstStep.getStartGLatLng());
            String buildingName = (building == null) ? "Outside" : building.getName();
            titleTextView.setText(buildingName);
        } else if (!mIsStarted) {
            circleImageView.setImageResource(R.drawable.ic_local_parking_black_24dp);
            titleTextView.setText(firstStep.getStartLocation().label);
        }


        // Determine whether it is action point for not
        if (isAction) {
            // Set default leg title
            titleTextView.setText(getStepName(firstStep, stepList.get(legStepCount - 1)));
            String[] actionNames = MapsIndoorsHelper.getActionNames();
            String firstStepHighWay = firstStep.getHighway();

            for (int idx = actionNames.length; --idx >= 0; ) {
                if (firstStepHighWay.equalsIgnoreCase(actionNames[idx])) {
                    circleImageView.setImageResource(mActionFileId[idx]);
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

            MPFilter filter = new MPFilter.Builder()
                    .setFloorIndex(0)
                    .setCategories(categories)
                    .build();

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


    private void addTransitOutSideSteps(int legIndex, int stepIndex, RouteStep previousTravelStep, List<RouteStep> stepList, LayoutInflater inflater, boolean didExitVenue) {
        boolean travelModeIsTransit = mSelectedTravelMode == MapsIndoorsHelper.VEHICLE_TRANSIT;

        //
        LinearLayout backgroundLayout = mBackgroundLayout;
        LinearLayout foregroundLayout = mForegroundLayout;

        View backgroundItem = inflater.inflate(R.layout.control_directions_menu_item_bg, null, true); // RelativeLayout backgroundItem = (RelativeLayout)inflater.inflate(R.layout.control_directions_menu_item_bg, null, true);
        View foregroundItem = inflater.inflate(R.layout.control_directions_menu_item_fg, null, true); // RelativeLayout foregroundItem = (RelativeLayout)inflater.inflate(R.layout.control_directions_menu_item_fg, null, true);
        backgroundLayout.addView(backgroundItem);
        foregroundLayout.addView(foregroundItem);

        int bgItemIndex = backgroundLayout.getChildCount() - 1;

        // =================================================================
        //
        //  ADD THE UI NAVIGATION HERE
        //
        // =================================================================
        final int selItemIndex = mUINaviList.size();

        UIRouteNavigation naviObj = generateNaviListObj(null, stepList, false);

        if (dbglog.isDeveloperMode()) {
            dbglog.Assert(naviObj != null, "UIRouteNavigation not created, reason: no points found in the stepList");
        }

        if (!travelModeIsTransit) {
            naviObj.set(false, legIndex, bgItemIndex);
        } else {
            naviObj.set(false, legIndex, stepIndex, bgItemIndex);
        }

        mUINaviList.add(naviObj);

        backgroundItem.setOnClickListener(v -> routeItemClickCallback(selItemIndex, true));

        // Get all subviews
        ImageView circleImageView = foregroundItem.findViewById(R.id.dir_horiz_circleImageView);
        ImageView travelActionImageView = foregroundItem.findViewById(R.id.dir_horiz_travelActionImageView);
        ImageView travelAction1ImageView = foregroundItem.findViewById(R.id.dir_horiz_travelAction1ImageView);
        ImageView travelCircleImageView = foregroundItem.findViewById(R.id.dir_horiz_travelCircleImageView);

        TextView titleTextView = foregroundItem.findViewById(R.id.dir_horiz_titleTextView);
        ImageView buslineView = backgroundItem.findViewById(R.id.dir_horiz_bus_line);

        TextView travelModeTextView = backgroundItem.findViewById(R.id.dir_horiz_travelModeTextView);
        ImageView travelModeImageView = backgroundItem.findViewById(R.id.dir_horiz_travelModeImageView);

        //
        RouteStep firstStep = stepList.get(0);
        final BuildingCollection bc = MapsIndoors.getBuildings();
        Building building = bc.getBuilding(firstStep.getStartGLatLng());
        int firstStepTravelMode = firstStep.getTravelModeVehicle();


        if (firstStepTravelMode != TravelMode.VEHICLE_BICYCLING
                && firstStepTravelMode != TravelMode.VEHICLE_DRIVING) {
            final View walkOutsideLineView = backgroundItem.findViewById(R.id.dir_horiz_walk_outside_line);
            walkOutsideLineView.setVisibility(View.VISIBLE);
        } else {
            final View driveBikeLineView = backgroundItem.findViewById(R.id.dir_horiz_drive_bike_outside_line);
            driveBikeLineView.setVisibility(View.VISIBLE);
        }

        // if enter or exit
        if (didExitVenue) {
            circleImageView.setImageResource(R.drawable.ic_vec_sig_exit);

            String bName = (building == null) ? "Outside" : building.getName();
            titleTextView.setText(bName);
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

        if ((transitDetails != null) && firstStepTravelMode == TravelMode.VEHICLE_TRANSIT) {
            buslineView.setVisibility(View.VISIBLE);
            travelAction1ImageView.setVisibility(View.VISIBLE);
            travelCircleImageView.setVisibility(View.VISIBLE);

            circleImageView.setVisibility(View.GONE);
            travelAction1ImageView.setImageResource(R.drawable.ic_bus_down_90);
            travelActionImageView.setScaleType(ImageView.ScaleType.FIT_END);

            if (transitDetails.getLine() != null) {
                String lineName = transitDetails.getLine().getShort_name();

                if (lineName == null) {
                    lineName = transitDetails.getLine().getName();
                }

                travelModeTextView.setVisibility(View.VISIBLE);
                travelModeImageView.setVisibility(View.VISIBLE);

                travelModeTextView.setText(lineName);

                // In case of not having a text color, set it to black
                travelModeTextView.setTextColor(MapsIndoorsRouteHelper.getTransitDetailsLineTextColor(mContext, transitDetails));

                // In case of not having a bg/line color, set it to grey
                int tdLineColor = MapsIndoorsRouteHelper.getTransitDetailsLineColor(mContext, transitDetails);
                travelModeTextView.setBackgroundColor(tdLineColor);
                //
                buslineView.setColorFilter(tdLineColor);

                travelAction1ImageView.setColorFilter(tdLineColor);

                // Get the default vehicle icon
                String vehicleIconURL = transitDetails.getLine().getVehicle().getLocal_icon();

                // If the local version is not present, use gmap's default
                if (TextUtils.isEmpty(vehicleIconURL)) {
                    vehicleIconURL = transitDetails.getLine().getVehicle().getIcon();
                }

                travelModeImageView.clearColorFilter();

                Picasso.get().load("http:" + vehicleIconURL).into(travelModeImageView);
            }

            if (transitDetails.getDeparture_stop() != null) {
                titleTextView.setText(transitDetails.getDeparture_stop().getName());
            }
        }

        if (previousTransitDetails != null && previousTravelStep.getTravelModeVehicle() == TravelMode.VEHICLE_TRANSIT) {
            circleImageView.setVisibility(View.GONE);
            travelActionImageView.setVisibility(View.VISIBLE);
            travelActionImageView.setImageResource(R.drawable.ic_bus_up_90);
            travelActionImageView.setScaleType(ImageView.ScaleType.FIT_START);


            if (previousTransitDetails.getArrival_stop() != null) {
                titleTextView.setText(previousTransitDetails.getArrival_stop().getName());
            }

            if (previousTransitDetails.getLine() != null) {
                // In case of not having a bg/line color, set it to grey
                travelActionImageView.setColorFilter(MapsIndoorsRouteHelper.getTransitDetailsLineColor(mContext, previousTransitDetails));
            }
        }

        // Start point
        if (mIsStarted) {
            //  mIsStarted = false;

            circleImageView.setVisibility(View.VISIBLE);
            travelActionImageView.setVisibility(View.GONE);
            travelAction1ImageView.setVisibility(View.GONE);

            String locationLabel = mOrigin.getLocationName(mActivity);
            String desc = mOrigin.getFormattedDetails(mActivity);

            if (desc != null) {

                locationLabel = String.format(Locale.US, "%s\n(%s)", locationLabel, desc);
            }

            titleTextView.setText(locationLabel);
        }
    }


    private String getStepName(RouteStep startStep, RouteStep endStep) {
        int startStepStartPointZIndex = startStep.getStartPoint().getZIndex();

        String startStepStartFloorName = startStep.getStartFloorName();

        String result = String.format("%s %s", getString(R.string.level),
                TextUtils.isEmpty(startStepStartFloorName) ? startStepStartPointZIndex : startStepStartFloorName);

        if (startStepStartPointZIndex == endStep.getEndPoint().getZIndex()) {
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

        String endStepEndFloorName = endStep.getEndFloorName();

        if (TextUtils.isEmpty(endStepEndFloorName)) {
            result = String.format("%s to %s", result, endStep.getEndPoint().getZIndex());
        } else {
            result = String.format("%s to %s", result, endStepEndFloorName);
        }

        return result;
    }

    /**
     * Remove all legs from the view leaving it blank
     */
    private void resetLegs() {
        mBackgroundLayout.removeAllViewsInLayout();
        mForegroundLayout.removeAllViewsInLayout();
        //   mIsStarted = true;
    }

    void routeNavigateToNext() {
        int naviStepsCount = mUINaviList.size();

        if (mCurrentRouteNaviIndex < (naviStepsCount - 1)) {
            routeNavigateToIndex(mCurrentRouteNaviIndex + 1, true);
        }
    }

    void routeNavigateToPrev() {
        if (mCurrentRouteNaviIndex >= 1) {
            routeNavigateToIndex(mCurrentRouteNaviIndex - 1, true);
        }
    }

    static final String ANALYTICS_HORIZONTAL_DIRECTION_PANEL_PARAM = "Horizontal";


    void routeItemClickCallback(int index, boolean animate) {
        mActivity.getUserPositionTrackingViewModel().stopTracking();
        routeNavigateToIndex(index, animate);
    }


    public void routeNavigateToIndex(int index, boolean animate) {
        // ===============================================
        long t1 = 0, t4 = 0;
        long t0 = System.currentTimeMillis();
        // ===============================================

        //region REPORT TO ANALYTICS
        {
            if (mCurrentRoute != null) {
                float routeNaviStepsCount = mUINaviList.size();
                if (routeNaviStepsCount > 0) {
                    float segmentPositionFactor = index / routeNaviStepsCount;
                    final Bundle eventParams = new Bundle();

                    eventParams.putFloat(getString(R.string.fir_param_Segment_Position_Factor), segmentPositionFactor);
                    eventParams.putString(getString(R.string.fir_param_Directions_Layout), ANALYTICS_HORIZONTAL_DIRECTION_PANEL_PARAM);

                    GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Directions_Route_Segment_Selected), eventParams);
                }
            } else {
                if (BuildConfig.DEBUG) {
                    dbglog.Assert(false, "mCurrentRoute is null");
                }
            }
        }
        //endregion

        mCurrentRouteNaviIndex = index;

        UIRouteNavigation currentNavObj = mUINaviList.get(index);

        int itemIndex = currentNavObj.bgViewIndex;

        boolean isOutDoorsStep = !currentNavObj.isIndoors;

        // Highlight the given element
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
            // Path's AABB
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

            // Only indoor paths will tilt/rotate the camera to align it with the path's entry points
            if (currentNavObj.alignWithPathEntryPoints) {
                // Start by using the default max zoom level
                float targetZoom = mGoogleMap.getMaxZoomLevel();

                if (mUINaviList.size() - 1 == index) {
                    mActivity.getSelectionManager().selectLocation(mDestination.getLocation(), false, false, false, false);
                }

                int tiltIndex = (targetTilt == MapsIndoorsSettings.MAP_TILT_WHEN_ZOOM_IS_LESS_OR_EQUAL_TO_10) ? 0 : 1;

                if (currentNavObj.gmapCameraPositionBuilder[tiltIndex] == null) {
                    // ===============================================
                    t1 = System.currentTimeMillis();
                    // ===============================================

                    //
                    dstCamPosBuilder = new CameraPosition.Builder();
                    dstCamPosBuilder
                            .target(boxCenter)
                            .zoom(targetZoom)
                            .tilt(targetTilt)
                            .bearing(targetBearing);

                    // Move the camera first to the given target, zoom and rotations, then use the leg's bounds
                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(dstCamPosBuilder.build()));

                    //
                    Projection prj = mGoogleMap.getProjection();
                    LatLng dstCalcTarget = prj.fromScreenLocation(mViewPortScreenCenter);

                    CameraUpdate currCamPosUpdate = CameraUpdateFactory.newCameraPosition(currCamPos);

                    // Use now the AABB calculated after rotating the path
                    {
                        // Back to the "current" position
                        mGoogleMap.moveCamera(currCamPosUpdate);

                        //
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                                currentNavObj.latLngBounds,
                                mMapViewPortScreenWidth, mMapViewPortScreenHeight, mViewPortPadding));

                        targetZoom = mGoogleMap.getCameraPosition().zoom;
                    }

                    // Back to the "current" position
                    mGoogleMap.moveCamera(currCamPosUpdate);

                    dstCamPosBuilder
                            .zoom(targetZoom)
                            .target(dstCalcTarget);

                    currentNavObj.gmapCameraPositionBuilder[tiltIndex] = dstCamPosBuilder;

                    // ===============================================
                    t4 = System.currentTimeMillis();
                    // ===============================================
                } else {
                    dstCamPosBuilder = currentNavObj.gmapCameraPositionBuilder[tiltIndex];
                }
            } else {
                // No tilt in outdoor paths
                if (currentNavObj.gmapCameraPositionBuilder[0] == null) {
                    // ===============================================
                    t1 = System.currentTimeMillis();
                    // ===============================================

                    //
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                            legBounds,
                            mMapViewPortScreenWidth, mMapViewPortScreenHeight, mViewPortPadding));

                    //
                    Projection prj = mGoogleMap.getProjection();
                    LatLng dstCalcTarget = prj.fromScreenLocation(mViewPortScreenCenter);
                    float newZoom = mGoogleMap.getCameraPosition().zoom;

                    // Back to the "current" position
                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(currCamPos));

                    //
                    dstCamPosBuilder = new CameraPosition.Builder()
                            .target(dstCalcTarget)
                            .zoom(newZoom);

                    currentNavObj.gmapCameraPositionBuilder[0] = dstCamPosBuilder;

                    // ===============================================
                    t4 = System.currentTimeMillis();
                    // ===============================================
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

        // ===============================================
        long t2 = System.currentTimeMillis();
        // ===============================================


        if (isOutDoorsStep && (currentNavObj.stepIndex >= 0)) {
            onStepSelected(currentNavObj.legIndex, currentNavObj.stepIndex, itemIndex);
        } else {
            onLegSelected(currentNavObj.legIndex, itemIndex);
        }

        // Directions panel: focus on the selected step/leg

        // ===============================================
        long t3 = System.currentTimeMillis();
        // ===============================================

        scrollTo(itemIndex);

        //===================================================================================
        if (dbglog.isDeveloperMode()) {
            long ct = System.currentTimeMillis();
            dbglog.Log(TAG, "routeNavigateToIndex( " + index + " ) - took (ms): " + (ct - t0));
            dbglog.Log(TAG, " - invoke onStepSelected(): " + (t3 - t2));
            dbglog.Log(TAG, " - invoke scrollTo():       " + (ct - t3));
            if (currentNavObj.alignWithPathEntryPoints) {
                dbglog.Log(TAG, " - invoke INDOOR calc:  " + (t4 - t1));
            } else {
                dbglog.Log(TAG, " - invoke OUTDOOR calc:  " + (t4 - t1));
            }
        }
        //===================================================================================
    }


    void showDestinationInfoWindow(int index) {
        // selecting the destination POI when selecting the last leg
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

    private int getLegIndexFromNaviList(int index) {
        if (mUINaviList.size() > 0) {
            UIRouteNavigation nObj = mUINaviList.get(index);

            if (nObj.stepIndex >= 0) {
                return nObj.legIndex;
            }
        }

        return 0;

    }

    private void resetNavigation() {
        mCurrentRouteNaviIndex = 0;

        scrollTo(0);
    }

    private void scrollTo(int itemIndex) {
        final HorizontalScrollView sv = mScrollingContainerLayout;

        ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);

        // Get the (predefined) scroll item width, in pixels
        float scrollItemWidth = getResources().getDimension(R.dimen.directions_horiz_item_width);

        // Get the current vp width, in pixels
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int winWidth = metrics.widthPixels;

        // Get half of the remainder of ...
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


    @Override
    public boolean onBackPressed() {
        if (isActive()) {
            closeAndOpenMenu();
        }

        return true;
    }
    //endregion


    //endregion

    /**
     * @param currRouteLeg
     * @param stepList
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
            // ERROR
            dbglog.Assert(false, "either currRouteLeg or stepList are null");
            return null;
        }

        // The AABB of the leg/step, also when from a rotated indoors path's OBB
        final LatLngBounds latLngBounds;

        // Path first points (non overlapping) used to align the camera so p0 -> p1 will face North
        RouteCoordinate entry_p0, entry_p1;
        entry_p0 = entry_p1 = null;

        //
        float entryHeadingAngle = 0;

        // Non overlapping points list, as x/y pairs

        int pointCount = inputPointList.size();

        if (pointCount >= 2) {
            mTmpRCPointList.clear();

            // Get a clean list, without 2D overlaps

            // For indoor paths, store all non overlapping
            if (isPathIndoors) {
                RouteCoordinate prevPnt = new RouteCoordinate(100, 100, 0);
                double prevLng, prevLat;
                prevLng = prevLat = 100;


                // First pass to just get the two entry points (entry_p0, entry_p1), used to set the camera orientation...
                for (RouteCoordinate pnt : inputPointList) {
                    double lng = pnt.getLng();
                    double lat = pnt.getLat();

                    // Skip points with same x,y positions (overlaps) and points being too close (within half a meter in both axis)
                    if ((Double.compare(lng, prevLng) != 0) && (Double.compare(lat, prevLat) != 0)) {
                        prevLng = lng;
                        prevLat = lat;

                        // Only do this heavy magic on indoor paths
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

                    // Odd case check
                    if (mTmpRCPointList.size() > 1) {
                        entry_p1 = mTmpRCPointList.get(1);
                    } else {
                        // Hack to get p1 from p0
                        entry_p1 = new RouteCoordinate(entry_p0.getLat() + 1, entry_p0.getLng(), 0);
                    }

                    // Calculate the path's entry point heading angle
                    double legHeading = SphericalUtil.computeHeading(entry_p0.getLatLng(), entry_p1.getLatLng());
                    entryHeadingAngle = (float) ((legHeading > 0) ? legHeading : (360.0 + legHeading));
                }


                mTmpRCPointList.clear();

                // Get an array out of the input points
                prevLng = prevLat = 100;

                // Remove duplicate points
                for (RouteCoordinate pnt : inputPointList) {
                    double lng = pnt.getLng();
                    double lat = pnt.getLat();

                    // Skip points with same x,y positions (overlaps) and points being too close (within half a meter in both axis)
                    if ((Double.compare(lng, prevLng) != 0) && (Double.compare(lat, prevLat) != 0)) {
                        prevLng = lng;
                        prevLat = lat;

                        mTmpRCPointList.add(pnt);
                    }
                }

                // Get the AABB's center of the path
                final LatLngBounds cBounds = MapsIndoorsHelper.getPathBounds(mTmpRCPointList);

                // Fixes MIAAND-769 (Routing Oddity on Android)
                final LatLng pathCenter = SphericalUtil.interpolate(cBounds.northeast, cBounds.southwest, 0.5f);

                // Using the calculated legHeading, rotate the path and recalculate its bounds and update the latlng bounds
                latLngBounds = MapsIndoorsHelper.getRotatedBoundsFromPath(mTmpRCPointList, pathCenter, -entryHeadingAngle);
            } else {
                latLngBounds = MapsIndoorsHelper.getPathBounds(inputPointList);
            }
        } else {
            latLngBounds = null;
        }

        return new UIRouteNavigation(entry_p0, entry_p1, latLngBounds, isPathIndoors, entryHeadingAngle);
    }

    public void setDirectionViewSelectedStepListener(RouteListener routeListener) {
        this.mRouteListener = routeListener;
    }
}
