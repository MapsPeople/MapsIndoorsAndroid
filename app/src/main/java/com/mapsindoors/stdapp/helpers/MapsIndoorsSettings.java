package com.mapsindoors.stdapp.helpers;

import java.util.Collections;
import java.util.List;

/**
 * @author Jose J Varó - Copyright © 2018 MapsPeople A/S. All rights reserved.
 */
public class MapsIndoorsSettings {

    /**
     * Results cap (on indoor results only) when using the full search
     */
    public static final int FULL_SEARCH_INDOORS_QUERY_RESULT_MAX_LENGTH = 25;


    /**
     * Google Places search from given location - radius (in meters)
     */
    public static final int GOOGLE_PLACES_API_AUTOCOMPLETE_QUERY_RADIUS = 40000;


    /**
     *
     */
    public static final float VENUE_TILE_LAYER_VISIBLE_START_ZOOM_LEVEL = 17f;

    /**
     * Camera tilting when animating the camera to an indoor location, when current zoom is <= 10
     */
    public static final int MAP_TILT_WHEN_ZOOM_IS_LESS_OR_EQUAL_TO_10 = 30;

    /**
     * Camera tilting when animating the camera to an indoor location, when current zoom is > 10
     */
    public static final int MAP_TILT_WHEN_ZOOM_IS_GREATER_TO_10 = 45;

    /**
     * Max distance to auto-select walking mode in routing
     */
    public static final int ROUTING_MAX_WALKING_DISTANCE_IN_METERS = 5000;

    /**
     * When a user selects a location from the main menu, a route is created to that location and CACHED.
     * <br>
     * The cached route will be invalidated after this time has elapsed (a new one will be created IF the user
     * navigates to the direction menu)
     */
    public static final int ROUTING_ROUTE_VALIDITY_MAX_TIME_IN_SECS = 30;


    /**
     * This camera padding is just to avoid the locations getting too close to the view's edges
     */
    public static final int DISPLAY_SEARCH_RESULTS_CAMERA_PADDING_IN_DP = 50;

    /**
     * (accessible with AppConfigManager.getCountryCodes())
     */
    public static final List<String> SEARCH_COUNTRIES_LIST = Collections.emptyList();

    public static final float NO_BEARING_AVAILABLE_VALUE = Float.MAX_VALUE;

    public static final float MAPSINDOORS_TILES_AVAILABLE_ZOOM_LEVEL = 15.0f;
}
