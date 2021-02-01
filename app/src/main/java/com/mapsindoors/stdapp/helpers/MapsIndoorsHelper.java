package com.mapsindoors.stdapp.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mapsindoors.mapssdk.Building;
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.Category;
import com.mapsindoors.mapssdk.CategoryCollection;
import com.mapsindoors.mapssdk.Highway;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.Maneuver;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.RouteCoordinate;
import com.mapsindoors.mapssdk.RouteLeg;
import com.mapsindoors.mapssdk.RouteProperty;
import com.mapsindoors.mapssdk.RouteStep;
import com.mapsindoors.mapssdk.SphericalUtil;
import com.mapsindoors.mapssdk.TravelMode;
import com.mapsindoors.mapssdk.Venue;
import com.mapsindoors.mapssdk.VenueCollection;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.apis.googleplaces.GooglePlacesClient;
import com.mapsindoors.stdapp.apis.googleplaces.listeners.ReverseGeoCodeResultListener;
import com.mapsindoors.stdapp.apis.googleplaces.models.ReverseGeocodeResult;
import com.mapsindoors.stdapp.apis.googleplaces.models.ReverseGeocodeResults;
import com.mapsindoors.stdapp.listeners.GenericObjectResultCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * MapsIndoorsHelper
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 01/21/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class MapsIndoorsHelper {
    private static final String TAG = MapsIndoorsHelper.class.getSimpleName();

    public static final String INSIDE_BUILDING = "InsideBuilding";
    public static final String OUTSIDE_ON_VENUE = "OutsideOnVenue";

    /**
     *
     */
    public static boolean DBG_DISABLE_EMBEDOUTSIDEONVENUESTEPS = false;

    /**
     * Default, false
     */
    public static boolean RETURN_NEW_LEG_OBJECT = false;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VEHICLE_WALKING, VEHICLE_BICYCLING, VEHICLE_DRIVING, VEHICLE_TRANSIT, VEHICLE_NONE})
    public @interface Vehicle {
    }

    public static final int VEHICLE_WALKING = 0;
    public static final int VEHICLE_BICYCLING = 1;
    public static final int VEHICLE_DRIVING = 2;
    public static final int VEHICLE_TRANSIT = 3;
    public static final int VEHICLE_NONE = 4;


    private static HashMap<String, Integer> sDirectionIcons;
    private static HashMap<String, String> sDirectionNames;
    private static HashMap<Integer, Integer> sTravelModeIcons;
    private static HashMap<Integer, String> sTravelModeNames;
    private static ArrayList<RouteStep> sTempRouteList;
    private static String[] sActionName;


    public static void init(@NonNull Context context) {
        if (sDirectionIcons == null) {
            Resources res = context.getResources();

            HashMap<String, Integer> directionIcons = sDirectionIcons = new HashMap<>();
            HashMap<String, String> directionNames = sDirectionNames = new HashMap<>();
            HashMap<Integer, Integer> travelModeIcons = sTravelModeIcons = new HashMap<>();
            HashMap<Integer, String> travelModeNames = sTravelModeNames = new HashMap<>();

            directionIcons.put("left", R.drawable.ic_maneuver_keep_left);
            directionIcons.put("right", R.drawable.ic_maneuver_keep_right);

            directionIcons.put(Maneuver.LEFT, R.drawable.ic_maneuver_turn_left);
            directionIcons.put(Maneuver.LEFT_SLIGHT, R.drawable.ic_maneuver_turn_slight_left);
            directionIcons.put(Maneuver.LEFT_SHARP, R.drawable.ic_maneuver_turn_sharp_left);
            directionIcons.put(Maneuver.RIGHT, R.drawable.ic_maneuver_turn_right);
            directionIcons.put(Maneuver.RIGHT_SLIGHT, R.drawable.ic_maneuver_turn_slight_right);
            directionIcons.put(Maneuver.RIGHT_SHARP, R.drawable.ic_maneuver_turn_sharp_right);
            directionIcons.put(Maneuver.STRAIGHT_AHEAD, R.drawable.ic_maneuver_straight);
            directionIcons.put(Maneuver.ROUNDABOUT_LEFT, R.drawable.ic_maneuver_roundabout_left);
            directionIcons.put(Maneuver.ROUNDABOUT_RIGHT, R.drawable.ic_maneuver_roundabout_right);
            directionIcons.put(Maneuver.RAMP_LEFT, R.drawable.ic_maneuver_ramp_left);
            directionIcons.put(Maneuver.RAMP_RIGHT, R.drawable.ic_maneuver_ramp_right);
            directionIcons.put(Maneuver.U_TURN, R.drawable.ic_maneuver_uturn_left);
            directionIcons.put(Maneuver.U_TURN_LEFT, R.drawable.ic_maneuver_uturn_left);
            directionIcons.put(Maneuver.U_TURN_RIGHT, R.drawable.ic_maneuver_uturn_right);
            directionIcons.put(Maneuver.MERGE, R.drawable.ic_maneuver_merge);
            directionIcons.put(Maneuver.FORK_LEFT, R.drawable.ic_maneuver_fork_left);
            directionIcons.put(Maneuver.FORK_RIGHT, R.drawable.ic_maneuver_fork_right);
            directionIcons.put(Maneuver.FERRY, R.drawable.ic_maneuver_ferry);
            directionIcons.put(Maneuver.FERRYTRAIN, R.drawable.ic_maneuver_ferry_train);
            directionIcons.put(Maneuver.KEEP_LEFT, R.drawable.ic_maneuver_keep_left);
            directionIcons.put(Maneuver.KEEP_RIGHT, R.drawable.ic_maneuver_keep_right);

            directionNames.put("left", res.getString(R.string.direction_keep_left));
            directionNames.put("right", res.getString(R.string.direction_keep_right));

            directionNames.put(Maneuver.LEFT, res.getString(R.string.direction_left));
            directionNames.put(Maneuver.LEFT_SLIGHT, res.getString(R.string.direction_slightly_left));
            directionNames.put(Maneuver.LEFT_SHARP, res.getString(R.string.direction_sharp_left));
            directionNames.put(Maneuver.RIGHT, res.getString(R.string.direction_right));
            directionNames.put(Maneuver.RIGHT_SLIGHT, res.getString(R.string.direction_slightly_right));
            directionNames.put(Maneuver.RIGHT_SHARP, res.getString(R.string.direction_sharp_right));
            directionNames.put(Maneuver.STRAIGHT_AHEAD, res.getString(R.string.direction_straight));
            directionNames.put(Maneuver.U_TURN, res.getString(R.string.direction_make_uturn));

            // These should be treated as an error instead (indoors shouldn't generate an u-turn...)
            directionNames.put(Maneuver.U_TURN_LEFT, res.getString(R.string.direction_make_uturn_left));
            directionNames.put(Maneuver.U_TURN_RIGHT, res.getString(R.string.direction_make_uturn_right));

            // Indoors only
            directionIcons.put(Maneuver.STRAIGHT_AHEAD_VIA_STAIRS, R.drawable.ic_stairs);
            directionNames.put(Maneuver.STRAIGHT_AHEAD_VIA_STAIRS, res.getString(R.string.direction_straight_via_stairs));


            travelModeIcons.put(TravelMode.VEHICLE_WALKING, R.drawable.ic_directions_walk);
            travelModeIcons.put(TravelMode.VEHICLE_BICYCLING, R.drawable.ic_directions_bike);
            travelModeIcons.put(TravelMode.VEHICLE_TRANSIT, R.drawable.ic_directions_bus);
            travelModeIcons.put(TravelMode.VEHICLE_DRIVING, R.drawable.ic_directions_car);

            travelModeNames.put(TravelMode.VEHICLE_WALKING, res.getString(R.string.travel_mode_walking));
            travelModeNames.put(TravelMode.VEHICLE_BICYCLING, res.getString(R.string.travel_mode_bicycling));
            travelModeNames.put(TravelMode.VEHICLE_TRANSIT, res.getString(R.string.travel_mode_transit));
            travelModeNames.put(TravelMode.VEHICLE_DRIVING, res.getString(R.string.travel_mode_driving));

            sTempRouteList = new ArrayList<>();
        }
    }

    public static boolean isStepInsideBuilding(@NonNull RouteStep step) {
        String abutters = step.getAbutters();
        return !TextUtils.isEmpty(abutters) && abutters.equalsIgnoreCase(INSIDE_BUILDING);
    }

    public static boolean isStepOutsideOnVenue(@NonNull RouteStep step) {
        String abutters = step.getAbutters();
        return !TextUtils.isEmpty(abutters) && abutters.equalsIgnoreCase(OUTSIDE_ON_VENUE);
    }

    public static boolean isStepFromGMaps(@NonNull RouteStep step) {
        return TextUtils.isEmpty(step.getAbutters());
    }


    public static boolean hasManeuverIcon(@Nullable String maneuverName) {
        if (!TextUtils.isEmpty(maneuverName)) {
            return sDirectionIcons.containsKey(maneuverName);
        } else {
            return false;
        }
    }


    public static boolean isStepOutside(@NonNull RouteStep step) {
        return isStepFromGMaps(step) || isStepOutsideOnVenue(step);
    }

    /**
     * @param maneuverName Any Maneuver name
     * @return The icon matching the given maneuver name or a generic (straight) one if ...
     */
    @DrawableRes
    public static int getManeuverIcon(@Nullable String maneuverName) {

        int res = R.drawable.ic_maneuver_straight;
        if (!TextUtils.isEmpty(maneuverName)) {
            Integer intRes = sDirectionIcons.get(maneuverName);
            if (intRes != null) {
                res = intRes;
            }
        }
        return res;
    }

    /**
     * @param maneuverName Any Maneuver name
     * @return
     */
    @NonNull
    public static String getManeuverInstructions(@Nullable String maneuverName) {
        String res;
        if (!TextUtils.isEmpty(maneuverName)) {
            res = sDirectionNames.get(maneuverName);
        } else {
            res = null;
        }
        return (res != null) ? res : "";
    }


    @DrawableRes
    public static int getTravelModeIcon(int travelMode) {
        int res = R.drawable.ic_directions_walk;
        if (travelMode >= 0) {
            Integer intRes = sTravelModeIcons.get(travelMode);
            if (intRes != null) {
                res = intRes;
            }
        }
        return res;
    }


    @NonNull
    public static String getTravelModeName(int travelMode) {
        String res;
        if (travelMode >= 0) {
            res = sTravelModeNames.get(travelMode);
        } else {
            res = null;
        }
        return (res != null) ? res : "";
    }

    @NonNull
    public static String[] getActionNames() {
        if (sActionName == null) {
            sActionName = new String[]{
                    Highway.ELEVATOR,
                    Highway.ESCALATOR,
                    Highway.STEPS,
                    Highway.TRAVELATOR
            };
        }

        return sActionName;
    }

    public static @Vehicle
    int travelModeToVehicle(@Nullable String travelMode) {
        if (!TextUtils.isEmpty(travelMode)) {
            switch (travelMode.toUpperCase(Locale.ROOT)) {
                case TravelMode.BICYCLING:
                    return VEHICLE_BICYCLING;
                case TravelMode.DRIVING:
                    return VEHICLE_DRIVING;
                case TravelMode.TRANSIT:
                    return VEHICLE_TRANSIT;
                default:
                    return VEHICLE_WALKING;
            }
        } else {
            // Default to Walk travel mode
            return VEHICLE_WALKING;
        }
    }

    /**
     * This "hacks" the received route leg data before generating the directions panel from it.
     * <br>
     * Ideally, this should be done in the library side, not here
     *
     * @param pLeg The route leg to be processed
     */
    @Nullable
    public static RouteLeg embedOutsideOnVenueSteps(RouteLeg pLeg, @Vehicle int selectedTravelMode) {
        RouteLeg leg;

        if (RETURN_NEW_LEG_OBJECT) {
            leg = new RouteLeg(pLeg);
        } else {
            leg = pLeg;
        }

        if (DBG_DISABLE_EMBEDOUTSIDEONVENUESTEPS) {
            return null;
        }

        // First, check if the leg is "outdoors"
        List<RouteStep> steps = leg.getSteps();
        int stepCount = steps.size();

        if (dbglog.isDeveloperMode()) {
            dbglog.Assert(stepCount > 0, "");
        }

        RouteStep firstStep = steps.get(0);
        RouteStep lastStep = steps.get(stepCount - 1);

        // Skip this whole "fix" if the whole leg is indoors
        final boolean isFirstStepInsideBuilding = MapsIndoorsHelper.isStepInsideBuilding(firstStep);
        if (isFirstStepInsideBuilding) {
            return null;
        }

        // Skip if the first or last steps are not tagged as "OutsideOnVenue"
        final boolean firstStep_Is_OutsideOnVenue = MapsIndoorsHelper.isStepOutsideOnVenue(firstStep);
        final boolean lastStep_Is_OutsideOnVenue = MapsIndoorsHelper.isStepOutsideOnVenue(lastStep);

        // Skip if the first and last steps are not tagged as "OutsideOnVenue"
        if (!firstStep_Is_OutsideOnVenue && !lastStep_Is_OutsideOnVenue) {
            return null;
        }

        // If the leg is not indoors, skip if the selected travel mode is WALKING
        final boolean travelModeIsWalking = selectedTravelMode == MapsIndoorsHelper.VEHICLE_WALKING;
        if (travelModeIsWalking) {
            return null;
        }

// TRANSIT
        sTempRouteList.clear();

        // Create a new step on this leg and move all current steps into it IF:
        // - ALL THE STEPS ARE OF TYPE "OutsideOnVenue" WHEN IN TRANSIT MODE
        boolean setAsIfFromGMaps = true;
        for (int i = 0; i < stepCount; i++) {
            if (MapsIndoorsHelper.isStepOutsideOnVenue(steps.get(i))) {
                continue;
            }
            setAsIfFromGMaps = false;
        }

        if (setAsIfFromGMaps) {

            if (steps.size() > 0) {
                sTempRouteList.addAll(steps);
                steps.removeAll(sTempRouteList);

                RouteStep extraStep = new RouteStep();
                extraStep.setTravelMode(TravelMode.WALKING);
                extraStep.setSteps(new ArrayList<>(sTempRouteList));
                steps.add(extraStep);
                sTempRouteList.clear();
            }
            return leg;
        }

        // Remove single point walks between commutes
        for (int j = 0; j < stepCount; j++) {

            final RouteStep step = steps.get(j);

            if (isStepFromGMaps(step)) {

                final List<RouteStep> subSteps = step.getSteps();
                if ((subSteps != null) && (subSteps.size() == 1)) {

                    final List<RouteCoordinate> stepGeometry = step.getGeometry();
                    if ((stepGeometry != null) && (stepGeometry.size() == 1)) // if( stepGeometry.size() < 2 )
                    {
                        sTempRouteList.add(step);
                    }
                }
            }
        }

        if (sTempRouteList.size() > 0) {
            steps.removeAll(sTempRouteList);
            stepCount = steps.size();
            sTempRouteList.clear();
        }

        // Move all the first "outsideonvenue" steps into the first gmap step's substep list
        if (firstStep_Is_OutsideOnVenue) {
            float accuDist, accuDur;
            accuDist = accuDur = 0f;

            // Add all the "outsideonvenue" steps found from beginning of the list onto a temp list
            // up to a gmap step is found
            for (int j = 0; j < stepCount; j++) {
                RouteStep step = steps.get(j);

                if (!MapsIndoorsHelper.isStepOutsideOnVenue(step)) {
                    break;
                }

                // Set this step as it was a gmap one
                step.setAbutters("");
                step.setHighway("");

                accuDist += step.getDistance();
                accuDur += step.getDuration();

                sTempRouteList.add(step);
            }

            if (sTempRouteList.size() > 0) {
                stepCount = steps.size();
                int toBeRemovedCount = sTempRouteList.size();

                if (stepCount > toBeRemovedCount) {
                    steps.removeAll(sTempRouteList);
                    stepCount = steps.size();

                    RouteStep newFirstStep = steps.get(0);
                    final List<RouteStep> lastStepSubSteps = newFirstStep.getSteps();

                    if (lastStepSubSteps != null) {
                        lastStepSubSteps.addAll(0, sTempRouteList);
                    } else {
                        newFirstStep.setSteps(new ArrayList<>(sTempRouteList));
                    }

                    // Update distance and duration props
                    RouteProperty distanceProp = newFirstStep.getDistanceProperty();
                    RouteProperty durationProp = newFirstStep.getDurationProperty();

                    // Sum distance and duration
                    // Note that we are ONLY updating the property VALUE, not its TEXT
                    if (distanceProp != null) {
                        distanceProp.setValue(distanceProp.getValue() + accuDist);
                    }
                    if (durationProp != null) {
                        durationProp.setValue(durationProp.getValue() + accuDur);
                    }
                } else {
                    if (dbglog.isDeveloperMode()) {
                        dbglog.Log(TAG, "");
                    }
                }
            }

            sTempRouteList.clear();
        }

        // Move all the "outsideonvenue" steps into the last step's substep list
        if (lastStep_Is_OutsideOnVenue) {

            float accuDist, accuDur;
            accuDist = accuDur = 0f;

            // Add all the "outsideonvenue" steps found from the end of the list onto a temp list
            // up to a gmap step is found
            for (int i = stepCount; --i >= 0; ) {
                RouteStep step = steps.get(i);

                if (!MapsIndoorsHelper.isStepOutsideOnVenue(step)) {
                    break;
                }

                // Set this step as it was a gmap one
                step.setAbutters("");
                step.setHighway("");

                accuDist += step.getDistance();
                accuDur += step.getDuration();

                sTempRouteList.add(step);
            }

            if (sTempRouteList.size() > 0) {
                Collections.reverse(sTempRouteList);

                stepCount = steps.size();
                int toBeRemovedCount = sTempRouteList.size();

                if (stepCount > toBeRemovedCount) {
                    steps.removeAll(sTempRouteList);
                    stepCount = steps.size();

                    RouteStep newLastStep = steps.get(stepCount - 1);
                    final List<RouteStep> lastStepSubSteps = newLastStep.getSteps();

                    if (lastStepSubSteps != null) {
                        lastStepSubSteps.addAll(sTempRouteList);

                        // Update distance and duration props
                        RouteProperty distanceProp = newLastStep.getDistanceProperty();
                        RouteProperty durationProp = newLastStep.getDurationProperty();

                        // Sum distance and duration
                        // Note that we are ONLY updating the property VALUE, not its TEXT
                        if (distanceProp != null) {
                            distanceProp.setValue(distanceProp.getValue() + accuDist);
                        }
                        if (durationProp != null) {
                            durationProp.setValue(durationProp.getValue() + accuDur);
                        }
                    } else {

                        RouteStep extraStep = new RouteStep();

                        extraStep.setTravelMode(TravelMode.WALKING);

                        // ========================================================================
                        // Note that RouteStep.setSteps() will sum-up distances and durations and
                        // do some other magic...
                        // =======================================================================
                        extraStep.setSteps(new ArrayList<>(sTempRouteList));

                        steps.add(extraStep);
                    }
                } else {
                    if (dbglog.isDeveloperMode()) {
                        dbglog.Log(TAG, "");
                    }
                }
            }

            sTempRouteList.clear();
        }

        return leg;
    }

    /**
     * 1+ consecutive Straight "OutsideOnVenue" steps will collapse into a sigle one (adding up dist / time)
     *
     * @param steps
     * @return
     */
    public static boolean optimizeOutsideOnVenueSteps(List<RouteStep> steps) {
        int stepCount = steps.size();
        int origStepCount = stepCount;

        //
        for (int i = 1; i < stepCount; i++) {
            RouteStep step = steps.get(i);

            // Skip if any has the highway set to steps
            if (step.getHighway().equalsIgnoreCase(Highway.STEPS)) {
                continue;
            }

            String abutters = step.getAbutters();
            if (!abutters.equalsIgnoreCase(OUTSIDE_ON_VENUE)) {
                continue;
            }

            int prevStepIndex = i - 1;
            RouteStep prevStep = steps.get(prevStepIndex);

            // Skip if any has the highway set to steps
            if (prevStep.getHighway().equalsIgnoreCase(Highway.STEPS)) {
                continue;
            }

            abutters = prevStep.getAbutters();

            if (!abutters.equalsIgnoreCase(OUTSIDE_ON_VENUE)) {
                continue;
            }

            final String currentStepManeuver = step.getManeuver();
            final String prevStepManeuver = prevStep.getManeuver();
            if ((currentStepManeuver == null) || (prevStepManeuver == null)) {
                continue;
            }
            if (!currentStepManeuver.equalsIgnoreCase(Maneuver.STRAIGHT_AHEAD)) {
                continue;
            }
            if (!currentStepManeuver.equalsIgnoreCase(prevStepManeuver)) {
                continue;
            }

            RouteProperty distanceProp = prevStep.getDistanceProperty();
            RouteProperty durationProp = prevStep.getDurationProperty();

            // Sum distance and duration
            // Note that we are ONLY updating the property VALUE, not its TEXT
            if (distanceProp != null) {
                float distance = distanceProp.getValue();
                distanceProp.setValue(distance + step.getDistance());
            }
            if (durationProp != null) {
                float duration = durationProp.getValue();
                durationProp.setValue(duration + step.getDuration());
            }

            // Remove the front step
            steps.remove(i);

            // Adjust current index and total count
            --i;
            --stepCount;
        }

        return origStepCount != stepCount;
    }

    @NonNull
    public static LatLngBounds getPathBounds(List<RouteCoordinate> pathPoints) {
        double minLng, minLat, maxLng, maxLat;
        minLng = minLat = 1000;
        maxLng = maxLat = -1000;

        int pointCount = pathPoints.size();

        // Run through the whole points list and get the ones used for a bbox calc only
        for (int i = 0; i < pointCount; i++) {
            RouteCoordinate pnt = pathPoints.get(i);
            double lng = pnt.getLng();
            double lat = pnt.getLat();

            if (lng < minLng) {
                minLng = lng;
            }
            if (lng > maxLng) {
                maxLng = lng;
            }
            if (lat < minLat) {
                minLat = lat;
            }
            if (lat > maxLat) {
                maxLat = lat;
            }
        }

        if (BuildConfig.DEBUG) {
            if ((Double.compare(minLng, 1000) == 0) || (Double.compare(minLat, 1000) == 0)
                    || (Double.compare(maxLng, -1000) == 0) || (Double.compare(maxLat, -1000) == 0)) {
                dbglog.Assert(false, "Error generating the path's AABB");
            }
        }

        // Build the AABB
        LatLngBounds.Builder latLngBuilder = LatLngBounds.builder();
        latLngBuilder.include(new LatLng(minLat, minLng));
        latLngBuilder.include(new LatLng(maxLat, maxLng));

        return latLngBuilder.build();
    }

    /**
     * Returns a new LatLng bounds object after rotating the given one by an angle
     *
     * @param pathPoints
     * @param angle
     * @return
     */
    @NonNull
    public static LatLngBounds getRotatedBoundsFromPath(List<RouteCoordinate> pathPoints, LatLng centerLatLng, double angle) {
        //
        RouteCoordinate centerP = new RouteCoordinate(centerLatLng.latitude, centerLatLng.longitude, 0);
        LatLngBounds.Builder latLngBuilder = LatLngBounds.builder();

        // Rotate the path points
        for (RouteCoordinate rc : pathPoints) {

            double a = centerP.bearing(rc);
            double d = centerP.distanceTo(rc);

            double newAngle = (a + angle);
            if (newAngle > 360) {
                newAngle -= 360;
            } else if (newAngle < 0) {
                newAngle += 360;
            }

            final LatLng newPoint = SphericalUtil.computeOffset(centerLatLng, d, newAngle);
            latLngBuilder.include(newPoint);
        }

        return latLngBuilder.build();
    }

    public static final int FORMAT_LOCATION_INFO_STRING_USE_COMMAS = 0;
    public static final int FORMAT_LOCATION_INFO_STRING_USE_LINE_BREAKS = 1;

    /**
     * Builds a string, describing a given location in detail (externalId, floor, building, venue).
     *
     * @param location
     * @param venueCollection
     * @param buildingCollection
     * @param itemSeperationFormat
     * @param includeExtId
     * @return string
     */
    @NonNull
    public static String composeLocationInfoString(@Nullable MPLocation location, @Nullable VenueCollection venueCollection, @Nullable BuildingCollection buildingCollection, int itemSeperationFormat, boolean includeExtId, @Nullable Context context) {
        if (location == null) {
            return "";
        }

        boolean hasExtId = false;
        boolean hasFloorName = false;
        boolean hasBuildingName = false;
        boolean hasVenueName = false;

        StringBuilder sb = new StringBuilder();

        final String itemSeparator;
        if (itemSeperationFormat == FORMAT_LOCATION_INFO_STRING_USE_COMMAS) {
            itemSeparator = ", ";
        } else if (itemSeperationFormat == FORMAT_LOCATION_INFO_STRING_USE_LINE_BREAKS) {
            itemSeparator = "\n";
        } else {
            itemSeparator = " ";
        }

        if (includeExtId && !TextUtils.isEmpty(location.getExternalId())) {
            sb.append(location.getExternalId());
            hasExtId = true;
        }

        if (!TextUtils.isEmpty(location.getFloorName())) {
            if (hasExtId) {
                if (sb.length() != 0) {
                    sb.append(itemSeparator);
                }
            }
            //If the linebreak is used per text. Write the hint for the value.
            if (context != null) {
                sb.append(context.getString(R.string.location_menu_info_floor, location.getFloorName()));
            } else {
                sb.append(location.getFloorName());
            }

            hasFloorName = true;
        }

        if (buildingCollection != null && !buildingCollection.getBuildings().isEmpty()) {
            final Building building = buildingCollection.getBuildingByAdminId(location.getBuilding());
            if (building != null && !TextUtils.isEmpty(building.getName())) {
                if (sb.length() != 0) {
                    sb.append(itemSeparator);
                }
                sb.append(building.getName());
                hasBuildingName = true;
            }
        }

        if (venueCollection != null && !venueCollection.getVenues().isEmpty()) {
            final Venue venue = venueCollection.getVenueByName(location.getVenue());
            if (venue != null && !TextUtils.isEmpty(venue.getName())) {
                if (sb.length() != 0) {
                    sb.append(itemSeparator);
                }
                sb.append(venue.getVenueInfo().getName());
                hasVenueName = true;
            }
        }

        if (!hasFloorName && !hasBuildingName && !hasVenueName) {
            return "";
        } else {
            return sb.toString();
        }
    }


    public static void getGooglePlacesAddressByPosition(Context mContext, @Nullable Point userCPos, @Nullable final GenericObjectResultCallback<String> genericObjectResultCallback) {

        if (userCPos == null || genericObjectResultCallback == null) {
            if (genericObjectResultCallback != null) {
                genericObjectResultCallback.onResultReady(null);
            }
            return;
        }

        LatLng userPos = userCPos.getLatLng();
        GooglePlacesClient mGooglePlacesClient = new GooglePlacesClient();

        // outside a building
        mGooglePlacesClient.getLatLngAddress(userPos, mContext.getString(R.string.google_maps_key), reverseGeocodeResults -> new Handler(mContext.getMainLooper()).post(() -> {

            if (reverseGeocodeResults == null) {
                genericObjectResultCallback.onResultReady(null);
                return;
            }

            String address = null;

            for (ReverseGeocodeResult geoResult : reverseGeocodeResults.results) {
                if (geoResult.formattedAddress != null) {
                    address = geoResult.formattedAddress;
                    break;
                }
            }

            genericObjectResultCallback.onResultReady(address);
        }));
    }

    public static String getTravelModeFromInt(int travelMode) {
        switch (travelMode) {
            case MapsIndoorsHelper.VEHICLE_WALKING:
                return TravelMode.WALKING;
            case MapsIndoorsHelper.VEHICLE_BICYCLING:
                return TravelMode.BICYCLING;
            case MapsIndoorsHelper.VEHICLE_DRIVING:
                return TravelMode.DRIVING;
            case MapsIndoorsHelper.VEHICLE_TRANSIT:
                return TravelMode.TRANSIT;
            case MapsIndoorsHelper.VEHICLE_NONE:
            default:
                return TravelMode.WALKING;
        }
    }

    public static @StringRes
    int getTravelMeanFromIntTravelMode(int travelMode) {
        switch (travelMode) {
            case MapsIndoorsHelper.VEHICLE_WALKING:
                return R.string.estimated_route_duration_by_means_walk;
            case MapsIndoorsHelper.VEHICLE_BICYCLING:
                return R.string.estimated_route_duration_by_means_by_bike;
            case MapsIndoorsHelper.VEHICLE_DRIVING:
                return R.string.estimated_route_duration_by_means_by_car;
            case MapsIndoorsHelper.VEHICLE_TRANSIT:
                return R.string.estimated_route_duration_by_means_transit;
            case MapsIndoorsHelper.VEHICLE_NONE:
            default:
                return R.string.estimated_route_duration_by_means_walk;
        }
    }

    @NonNull
    public static String[] getLocationCategoryNames(@NonNull MPLocation location) {
        final CategoryCollection categoryCollection = MapsIndoors.getCategories();
        if (categoryCollection == null) {
            return new String[0];
        }

        // Get all category keys for the given location...
        final String[] locCategories = location.getCategories();
        if (locCategories == null) {
            return new String[0];
        }

        final List<Category> categoryList = categoryCollection.getCategories();
        final List<String> catNames = new ArrayList<>(locCategories.length);

        for (final String catKey : locCategories) {
            for (final Category category : categoryList) {
                if (catKey.equalsIgnoreCase(category.getKey())) {
                    catNames.add(category.getValue());
                }
            }
        }

        return catNames.toArray(new String[0]);
    }
}
