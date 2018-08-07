package com.mapsindoors.stdapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.mapsindoors.stdapp.BuildConfig;

/**
 * SharedPrefsHelper
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 12/4/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class SharedPrefsHelper {
    private static final String SETTINGS_PREFS_FILE_NAME = BuildConfig.APPLICATION_ID + ".SettingsSharedPreferences";
    private static final String SETTINGS_PREFS_KEY_VENUES_FIRST_SHOWN           = "VenuesFirstShown";
    private static final String SETTINGS_PREFS_KEY_APP_VERSION                  = "AppVersion";
    private static final String SETTINGS_PREFS_KEY_CURRENT_VENUE_ID             = "CurrentVenueID";
    private static final String SETTINGS_PREFS_KEY_AVOID_STAIRS                 = "OptAvoidStairs";
    private static final String SETTINGS_PREFS_KEY_USER_CHOOSEN_VENUE           = "ChoosenVenue";
    private static final String SETTINGS_PREFS_KEY_USER_CHOOSEN_TRAVEL_MODE     = "TravelMode";
    private static final String SETTINGS_PREFS_KEY_SHOW_ZOOM_BUTTON             = "zoomForDetailsButtonVisibility";

    private static final String API_KEY_VALIDITY     = "apiKeyValidity";


    //region App settings


    public static boolean hasUserChoosenAVenue(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);

        boolean hasChoosen = sp.getBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_USER_CHOOSEN_VENUE, false);

        return hasChoosen;
    }


    public static void setUserHasChoosenVenue(Context context, @NonNull boolean state) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_USER_CHOOSEN_VENUE, state);
        editor.apply();
    }

    //SETTINGS_PREFS_KEY_APP_VERSION
    public static void setAppVersionName(Context context, @NonNull String appVersionName) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SharedPrefsHelper.SETTINGS_PREFS_KEY_APP_VERSION, appVersionName);
        editor.apply();
    }

    public static String getAppVersionName(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);

        return sp.getString(SharedPrefsHelper.SETTINGS_PREFS_KEY_APP_VERSION, "");
    }
    //
    public static void setUserTravelingMode(Context context, @MapsIndoorsHelper.Vehicle int travelingMode) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SharedPrefsHelper.SETTINGS_PREFS_KEY_USER_CHOOSEN_TRAVEL_MODE, travelingMode);
        editor.apply();
    }


    public static @MapsIndoorsHelper.Vehicle int getUserTravelingMode(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);

        return sp.getInt(SharedPrefsHelper.SETTINGS_PREFS_KEY_USER_CHOOSEN_TRAVEL_MODE, MapsIndoorsHelper.VEHICLE_NONE);
    }

    /**
     * Stores the given venue id
     *
     * @param context
     * @param venueId
     */
    public static void setCurrentVenueId(Context context, @NonNull String venueId) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SharedPrefsHelper.SETTINGS_PREFS_KEY_CURRENT_VENUE_ID, venueId);
        editor.apply();
    }

    /**
     * @param context
     * @return The last venue id set by the user, empty string if nothing has been saved yet
     */
    @NonNull
    public static String getCurrentVenueId(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);

        return sp.getString(SharedPrefsHelper.SETTINGS_PREFS_KEY_CURRENT_VENUE_ID, "");
    }


    public static void setAvoidStairs(Context context, boolean avoidStairs) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_AVOID_STAIRS, avoidStairs);
        editor.apply();
    }


    public static boolean getAvoidStairs(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);

        return sp.getBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_AVOID_STAIRS, false);
    }

    public static void setZoomForDetailButtonToShow(Context context, boolean buttonToShow) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_SHOW_ZOOM_BUTTON, buttonToShow);
        editor.apply();
    }


    public static boolean isZoomForDetailButtonToShow(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_SHOW_ZOOM_BUTTON, true);
    }
    //endregion

    public static void setApiKeyValidity(Context context, boolean keyValidity) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsHelper.API_KEY_VALIDITY, keyValidity);
        editor.apply();
    }


    public static boolean getApiKeyValidity(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(SharedPrefsHelper.SETTINGS_PREFS_FILE_NAME, Context.MODE_PRIVATE);

        return sp.getBoolean(SharedPrefsHelper.API_KEY_VALIDITY, true);
    }

}
