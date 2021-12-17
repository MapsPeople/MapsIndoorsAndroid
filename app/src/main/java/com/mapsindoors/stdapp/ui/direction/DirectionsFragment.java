package com.mapsindoors.stdapp.ui.direction;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


import com.mapsindoors.mapssdk.AgencyInfo;
import com.mapsindoors.mapssdk.Building;
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.LineInfo;
import com.mapsindoors.mapssdk.MPFilter;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPQuery;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.Route;
import com.mapsindoors.mapssdk.RouteLeg;
import com.mapsindoors.mapssdk.RouteStep;
import com.mapsindoors.mapssdk.TransitDetails;
import com.mapsindoors.mapssdk.TravelMode;
import com.mapsindoors.mapssdk.dbglog;

import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsRouteHelper;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.models.UIRouteNavigation;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.direction.models.RoutingEndPoint;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Fragment that shows routes on the map (using mDirectionsRenderer)
 */
public abstract class DirectionsFragment extends BaseFragment  {

    protected Route mCurrentRoute;
    protected MapsIndoorsActivity mActivity;
    protected Context mContext;
    protected MapControl mMapControl;

    protected final int[] mAction = {R.string.action_message_elevator, R.string.action_message_escalator, R.string.action_message_stairs,
            R.string.action_message_travelator, R.string.action_message_ramp, R.string.action_message_wheelchairlift, R.string.action_message_wheelchairramp, R.string.action_message_ladder};
    private final int[] mActionFileId = {R.drawable.ic_vec_sig_lift, R.drawable.ic_vec_sig_escalator, R.drawable.ic_vec_sig_stairs, R.drawable.ic_vec_sig_stairs, R.drawable.misdk_ic_ramp, R.drawable.misdk_ic_wheelchairlift, R.drawable.misdk_ic_wheelchairramp, R.drawable.misdk_ic_ladder};


    protected RoutingEndPoint mOrigin, mDestination;

    /**
     * On a new route, the travel mode selected
     */
    protected @MapsIndoorsHelper.Vehicle
    int mSelectedTravelMode;

    protected boolean mIsLegIndoors, mIsStarted;
    private boolean mDidExit;

    protected List<AgencyInfo> mTransportAgencies;

    protected ArrayList<RouteStep> mTempRouteStepsList;

    /**
     * Navigation object, to be used by the prev/next buttons
     */
    protected List<UIRouteNavigation> mUINaviList;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUINaviList = new ArrayList<>();
        mTransportAgencies = new ArrayList<>();
        mTempRouteStepsList = new ArrayList<>();

        setupView(view);
    }

    /**
     * Abstract method for the subclasses.
     * @param view the view that is initialized in onViewCreated
     */
    abstract protected void setupView(View view);

    /**
     * Superclass method that is used by both subclasses, it sets up analytics for the route.
     * @param index the index of the route
     * @param animate whether the route should be animated, not used here, but is required in the
     *                body of the method
     */
    protected void routeNavigateToIndex(int index, boolean animate) {
        if (mCurrentRoute != null) {
            float routeNaviStepsCount = mUINaviList.size();
            if (routeNaviStepsCount > 0) {
                float segmentPositionFactor = index / routeNaviStepsCount;
                final Bundle eventParams = new Bundle();

                eventParams.putFloat(getString(R.string.fir_param_Segment_Position_Factor), segmentPositionFactor);
                if (this instanceof DirectionsVerticalFragment){
                    eventParams.putString(getString(R.string.fir_param_Directions_Layout), "Vertical");
                } else {
                    eventParams.putString(getString(R.string.fir_param_Directions_Layout), "Horizontal");
                }
                GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Directions_Route_Segment_Selected), eventParams);
            }
        } else {
            if (BuildConfig.DEBUG) {
                dbglog.Assert(false, "mCurrentRoute is null");
            }
        }
    }

    /**
     * Determines whether a RouteStep contains an Action
     * @param firstStep The current RouteStep
     * @param stepList A list of all available steps
     * @param titleTextView The text that describes the destination of the current leg
     * @param legStepCount The amount of steps in the leg
     * @param circleImageView The circle that shows the destination of the current leg
     * @param prefixTextView Prefix text, only shown in Vertical mode
     */
    protected void determineActionPoint(RouteStep firstStep, List<RouteStep> stepList, TextView titleTextView, int legStepCount, ImageView circleImageView, @Nullable TextView prefixTextView) {

        String[] actionNames = MapsIndoorsHelper.getActionNames();
        String firstStepHighWay = firstStep.getHighway();

        for (int idx = actionNames.length; --idx >= 0; ) {
            if (firstStepHighWay.equalsIgnoreCase(actionNames[idx])) {
                circleImageView.setImageResource(mActionFileId[idx]);
                if (this instanceof DirectionsVerticalFragment) {
                    titleTextView.setText(mContext.getString(R.string.action_message_take) + " "
                            + mContext.getString(mAction[idx]) + " "
                            + mContext.getString(R.string.action_message_to) + " "
                            + mContext.getString(R.string.level).toLowerCase(Locale.ROOT) + " "
                            + stepList.get(legStepCount - 1).getEndFloorName());
                    prefixTextView.setText(mContext.getString(R.string.prefix_action));
                } else {
                    titleTextView.setText(getStepName(firstStep, stepList.get(legStepCount - 1)));

                }
                break;
            }
        }
    }

    /**
     * Determines whether a RouteStep contains an Action
     * @param firstStep
     * @param titleTextView
     */
    void determineActionPoint(RouteStep firstStep, TextView titleTextView) {
        MPQuery query = new MPQuery.Builder()
                .setQuery("Parking")
                .setNear(firstStep.getStartPoint())
                .build();

        ArrayList<String> categories = new ArrayList<>();
        categories.add("Parking");
        MPFilter filter = new MPFilter.Builder().setFloorIndex(0).setCategories(categories).build();
        MapsIndoors.getLocationsAsync(query, filter, (list, miError) -> {
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
        });
    }

    /**
     * Determines whether the RouteStep exits/enters a building, and sets the UI to show this
     * @param firstStep
     * @param didContextChange
     * @param isCurrentLegIndoors
     * @param titleTextView
     * @param circleImageView
     * @param prefixTextView
     */
    void determineEnterOrExit(RouteStep firstStep, boolean didContextChange, boolean isCurrentLegIndoors, TextView titleTextView, ImageView circleImageView, @Nullable TextView prefixTextView) {
        if (didContextChange) {
            circleImageView.setImageResource(isCurrentLegIndoors ? R.drawable.ic_vec_sig_enter : R.drawable.ic_vec_sig_exit);
            if (this instanceof DirectionsVerticalFragment) {
                prefixTextView.setText(isCurrentLegIndoors ? getString(R.string.prefix_enter) : getString(R.string.prefix_exit));
            }
            final BuildingCollection bc = MapsIndoors.getBuildings();
            Building building = bc.getBuilding(firstStep.getStartGLatLng());
            String buildingName = (building == null) ? "Outside" : building.getName();
            titleTextView.setText(buildingName);
        } else if (!mIsStarted) {
            circleImageView.setImageResource(R.drawable.ic_local_parking_black_24dp);
            titleTextView.setText(firstStep.getStartLocation().label);
            if (this instanceof DirectionsVerticalFragment) {
                prefixTextView.setText(mContext.getString(R.string.prefix_park));
            }
        }
    }

    /**
     * Changes the TravelMode Icon
     * @param firstStep
     * @param item
     */
    void setTravelModeIcon(RouteStep firstStep, View item) {
        int id;
        if (this instanceof DirectionsVerticalFragment) {
            id = R.id.travelModeImageView;
        } else {
            id = R.id.dir_horiz_travelModeImageView;
        }

        ImageView travelModeImageView = item.findViewById(id);
        if (travelModeImageView != null) {
            int travelModeiconRes = MapsIndoorsHelper.getTravelModeIcon(firstStep.getTravelModeVehicle());
            travelModeImageView.setImageResource(travelModeiconRes);
            travelModeImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.dir_panel_travelmode_icon_tint));
        }
    }

    /**
     * Formats the POI text with the name and details about the POI
     * @param routingEndPoint
     * @param isDestination
     * @return The formatted text
     */
    String formatPOIText(RoutingEndPoint routingEndPoint, boolean isDestination) {

        String name = routingEndPoint.getLocationName(mActivity);
        String formattedDetails = routingEndPoint.getFormattedDetails(mActivity);

        if (name.equals(getString(R.string.my_position))) {
            return name;
        }

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

    /**
     *
     * @param startStep
     * @param endStep
     * @return
     */
    protected String getStepName(RouteStep startStep, RouteStep endStep) {
        int startStepStartPointZIndex = startStep.getStartPoint().getZIndex();

        String startStepStartFloorName = startStep.getStartFloorName();

        String result = String.format("%s %s", getString(R.string.level),
                TextUtils.isEmpty(startStepStartFloorName) ? startStepStartPointZIndex : startStepStartFloorName);

        if (startStepStartPointZIndex == endStep.getEndPoint().getZIndex()) {
            return result;
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
     *
     * @param currentRouteLeg
     * @param legIndex
     */
    protected void addLeg(RouteLeg currentRouteLeg , int legIndex) {

        if (this instanceof DirectionsVerticalFragment) {
            RouteLeg daLeg = currentRouteLeg;
            currentRouteLeg = MapsIndoorsHelper.embedOutsideOnVenueSteps(daLeg, mSelectedTravelMode);

            if (currentRouteLeg == null) {
                currentRouteLeg = daLeg;
            }
        }

        List<RouteStep> stepList = currentRouteLeg.getSteps();
        RouteStep firstStep = stepList.get(0);
        int legStepCount = stepList.size();

        final int startLevel = Objects.requireNonNull(currentRouteLeg.getStartPoint()).getZIndex();
        int endLevel = Objects.requireNonNull(currentRouteLeg.getEndPoint()).getZIndex();
        boolean isAction = (startLevel != endLevel);

        boolean isCurrentLegIndoors = MapsIndoorsHelper.isStepInsideBuilding(firstStep);

        boolean didContextChange = (mIsLegIndoors != isCurrentLegIndoors);

        mIsStarted = legIndex == 0;
        mDidExit = didContextChange && !isCurrentLegIndoors;
        mIsLegIndoors = isCurrentLegIndoors;

        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (!isCurrentLegIndoors && mSelectedTravelMode == TravelMode.VEHICLE_TRANSIT) {
            addTransitLeg(firstStep, legStepCount, stepList, legIndex, inflater);
            return;
        }

        addLegUI(legIndex, currentRouteLeg, stepList, firstStep, didContextChange, isCurrentLegIndoors, legStepCount, isAction, inflater);
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
    abstract protected void addLegUI(int legIndex, RouteLeg currentRouteLeg, List<RouteStep> stepList, RouteStep firstStep, Boolean didContextChange, Boolean isCurrentLegIndoors, int legStepCount, Boolean isAction, LayoutInflater inflater);

    /**
     *
     * @param firstStep
     * @param legStepCount
     * @param stepList
     * @param legIndex
     * @param inflater
     */
    private void addTransitLeg(RouteStep firstStep, int legStepCount, List<RouteStep> stepList, int legIndex, LayoutInflater inflater) {
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
                    || !(nextTravelStepTM == currentStepTravelMode)) {
                addTransitOutsideSteps(legIndex, i, previousTravelStep, new ArrayList<>(mTempRouteStepsList), inflater, mDidExit);

                mDidExit = false;

                previousTravelStep = currentTravelStep;
                mTempRouteStepsList.clear();

                if (!isLastStepInLeg) {
                    currentTravelStep = nextStep;
                }
            }
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
    abstract protected void addTransitOutsideSteps(int legIndex, int stepIndex, RouteStep previousTravelStep, List<RouteStep> stepList, LayoutInflater inflater, boolean didExitVenue);

    /**
     *
     * @param previousTravelStep
     * @param stepList
     * @param foregroundItem
     * @param backgroundItem
     * @param didExitVenue
     */
    protected void addTransitOutsideStepsUI(RouteStep previousTravelStep, List<RouteStep> stepList, View foregroundItem, @Nullable View backgroundItem, Boolean didExitVenue) {
        int walkOutsideRes;
        int bikeOutsideRes;
        int travelBus1;
        int travelBus2;
        ImageView circleImageView;
        ImageView travelActionImageView;
        ImageView travelAction1ImageView;
        ImageView travelCircleImageView;
        TextView titleTextView;
        ImageView buslineView;
        TextView travelModeTextView;
        ImageView travelModeImageView;
        TextView prefixTextView = null;
        TextView distanceTextView = null;
        TextView stopsTextView = null;
        LinearLayout directionTitleLinearLayout = null;
        View item;

        if (this instanceof DirectionsVerticalFragment) {
            walkOutsideRes = R.id.walk_outside_line;
            bikeOutsideRes = R.id.drive_bike_outside_line;
            travelBus1 = R.drawable.ic_bus_up;
            travelBus2 = R.drawable.ic_bus_down;

            circleImageView = foregroundItem.findViewById(R.id.circleImageView);
            travelActionImageView = foregroundItem.findViewById(R.id.travelActionImageView);
            travelAction1ImageView = foregroundItem.findViewById(R.id.travelAction1ImageView);
            travelCircleImageView = foregroundItem.findViewById(R.id.travelCircleImageView);
            titleTextView = foregroundItem.findViewById(R.id.titleTextView);
            buslineView = foregroundItem.findViewById(R.id.bus_line);
            travelModeTextView = foregroundItem.findViewById(R.id.travelModeTextView);
            travelModeImageView = foregroundItem.findViewById(R.id.travelModeImageView);

            prefixTextView = foregroundItem.findViewById(R.id.prefixTextView);
            distanceTextView = foregroundItem.findViewById(R.id.distanceTextView);
            stopsTextView = foregroundItem.findViewById(R.id.stopsTextView);
            directionTitleLinearLayout = foregroundItem.findViewById(R.id.directionTitleLinearLayout);

            item = foregroundItem;

        } else {
            walkOutsideRes = R.id.dir_horiz_walk_outside_line;
            bikeOutsideRes = R.id.dir_horiz_drive_bike_outside_line;
            travelBus1 = R.drawable.ic_bus_down_90;
            travelBus2 = R.drawable.ic_bus_up_90;

            circleImageView = foregroundItem.findViewById(R.id.dir_horiz_circleImageView);
            travelActionImageView = foregroundItem.findViewById(R.id.dir_horiz_travelActionImageView);
            travelAction1ImageView = foregroundItem.findViewById(R.id.dir_horiz_travelAction1ImageView);
            travelCircleImageView = foregroundItem.findViewById(R.id.dir_horiz_travelCircleImageView);
            titleTextView = foregroundItem.findViewById(R.id.dir_horiz_titleTextView);

            buslineView = backgroundItem.findViewById(R.id.dir_horiz_bus_line);
            travelModeTextView = backgroundItem.findViewById(R.id.dir_horiz_travelModeTextView);
            travelModeImageView = backgroundItem.findViewById(R.id.dir_horiz_travelModeImageView);

            item = backgroundItem;
        }

        if (mIsStarted) {
            circleImageView.setVisibility(View.VISIBLE);
            travelActionImageView.setVisibility(View.GONE);
            travelAction1ImageView.setVisibility(View.GONE);
            titleTextView.setText(formatPOIText(mOrigin, false));
            if (this instanceof DirectionsVerticalFragment) {
                prefixTextView.setText(getString(R.string.prefix_start));
            }
        }

        RouteStep firstStep = stepList.get(0);
        final BuildingCollection bc = MapsIndoors.getBuildings();
        Building building = bc.getBuilding(firstStep.getStartGLatLng());
        int firstStepTravelMode = firstStep.getTravelModeVehicle();


        if (firstStepTravelMode != TravelMode.VEHICLE_BICYCLING
                && firstStepTravelMode != TravelMode.VEHICLE_DRIVING) {
            final View walkOutsideLineView = item.findViewById(walkOutsideRes);
            walkOutsideLineView.setVisibility(View.VISIBLE);
        } else {
            final View driveBikeLineView = item.findViewById(bikeOutsideRes);
            driveBikeLineView.setVisibility(View.VISIBLE);
        }

        if (didExitVenue) {
            circleImageView.setImageResource(R.drawable.ic_vec_sig_exit);
            if (this instanceof DirectionsVerticalFragment) {
                prefixTextView.setText(getString(R.string.prefix_exit));
            }

            String buildingName = (building == null) ? "Outside" : building.getName();
            titleTextView.setText(buildingName);
        }

        travelModeTextView.setText(MapsIndoorsHelper.getTravelModeName(firstStepTravelMode));

        if (travelModeImageView != null) {
            int travelModeIconRes = MapsIndoorsHelper.getTravelModeIcon(firstStepTravelMode);
            travelModeImageView.setImageResource(travelModeIconRes);
            travelModeImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.dir_panel_travelmode_icon_tint));
        }

        TransitDetails transitDetails = firstStep.getTransitDetails();
        TransitDetails previousTransitDetails = previousTravelStep.getTransitDetails();

        if (transitDetails != null && firstStepTravelMode == TravelMode.VEHICLE_TRANSIT) {
            buslineView.setVisibility(View.VISIBLE);
            travelAction1ImageView.setVisibility(View.VISIBLE);
            travelCircleImageView.setVisibility(View.VISIBLE);

            circleImageView.setVisibility(View.GONE);
            travelAction1ImageView.setImageResource(travelBus1);
            travelActionImageView.setScaleType(ImageView.ScaleType.FIT_END);

            LineInfo transitDetailsLineInfo = transitDetails.getLine();
            if (transitDetailsLineInfo != null) {
                String lineName = transitDetailsLineInfo.getShort_name();

                if (lineName == null) {
                    lineName = transitDetailsLineInfo.getName();
                }

                if (transitDetailsLineInfo.getAgencies() != null && this instanceof DirectionsVerticalFragment) {
                    mTransportAgencies.addAll(transitDetailsLineInfo.getAgencies());
                }

                travelModeTextView.setVisibility(View.VISIBLE);
                travelModeImageView.setVisibility(View.VISIBLE);

                travelModeTextView.setText(lineName);

                travelModeTextView.setTextColor(MapsIndoorsRouteHelper.getTransitDetailsLineTextColor(mContext, transitDetails));

                int tdLineColor = MapsIndoorsRouteHelper.getTransitDetailsLineColor(mContext, transitDetails);
                travelModeTextView.setBackgroundColor(tdLineColor);

                buslineView.setColorFilter(tdLineColor);

                travelAction1ImageView.setColorFilter(tdLineColor);

                String vehicleIconURL = transitDetailsLineInfo.getVehicle().getLocal_icon();

                if (TextUtils.isEmpty(vehicleIconURL)) {
                    vehicleIconURL = transitDetailsLineInfo.getVehicle().getIcon();
                }

                travelModeImageView.clearColorFilter();

                Picasso.get().load("http:" + vehicleIconURL).into(travelModeImageView);


            }

            if (transitDetails.getDeparture_stop() != null) {
                titleTextView.setText(transitDetails.getDeparture_stop().getName());
                if (this instanceof DirectionsVerticalFragment) {
                    prefixTextView.setText(getString(R.string.prefix_enter));
                }
            }

            if (this instanceof DirectionsVerticalFragment) {
                distanceTextView.setText(transitDetails.getHeadsign());
                directionTitleLinearLayout.setVisibility(View.GONE);
                stopsTextView.setVisibility(View.VISIBLE);
                String stopStr = (transitDetails.getNum_stops() >= 1) ? getResources().getString(R.string.stops) : getResources().getString(R.string.stop);
                float duration;
                duration = firstStep.getDuration();

                stopsTextView.setText(String.format(Locale.US, "%d %s (%s)", transitDetails.getNum_stops(), stopStr, MapsIndoorsRouteHelper.getFormattedDuration((int) duration)));
            }
        }

        if (previousTransitDetails != null && previousTravelStep.getTravelModeVehicle() == TravelMode.VEHICLE_TRANSIT) {
            circleImageView.setVisibility(View.GONE);
            travelActionImageView.setVisibility(View.VISIBLE);
            travelActionImageView.setImageResource(travelBus2);
            travelActionImageView.setScaleType(ImageView.ScaleType.FIT_START);

            if (previousTransitDetails.getArrival_stop() != null) {
                titleTextView.setText(previousTransitDetails.getArrival_stop().getName());
                if (this instanceof DirectionsVerticalFragment) {
                    prefixTextView.setText(getString(R.string.prefix_exit));
                }
            }

            if (previousTransitDetails.getLine() != null) {
                travelActionImageView.setColorFilter(MapsIndoorsRouteHelper.getTransitDetailsLineColor(mContext, previousTransitDetails));
            }
        }
    }
}
