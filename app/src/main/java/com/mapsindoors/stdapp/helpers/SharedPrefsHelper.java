package com.mapsindoors.stdapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * SharedPrefsHelper
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 12/4/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class SharedPrefsHelper {
    /**
     * An initial value is given
     */
    private static final String SETTINGS_PREFS_BASE_FILE_NAME = "stdappPrefs";

    private static final String SETTINGS_PREFS_KEY_VENUES_FIRST_SHOWN = "VenuesFirstShown";
    private static final String SETTINGS_PREFS_KEY_APP_VERSION = "AppVersion";
    private static final String SETTINGS_PREFS_KEY_CURRENT_VENUE_ID = "CurrentVenueID";
    private static final String SETTINGS_PREFS_KEY_AVOID_STAIRS = "OptAvoidStairs";
    private static final String SETTINGS_PREFS_KEY_USER_CHOSEN_VENUE = "ChosenVenue";
    private static final String SETTINGS_PREFS_KEY_USER_CHOSEN_TRAVEL_MODE = "TravelMode";
    private static final String SETTINGS_PREFS_KEY_SHOW_ZOOM_BUTTON = "zoomForDetailsButtonVisibility";
    private static final String SETTINGS_PREFS_KEY_SOLUTION_ALIAS_KEY = "solutionAlias";
    private static final String SETTINGS_PREFS_KEY_SELECTED_USER_ROLES = "SelUserRoles";
    private static final String SETTINGS_PREFS_KEY_API_KEY_VALIDITY = "apiKeyValidity";

    private static String settingsPrefsBaseFileName;

    /**
     * Sets the Shared prefs filename's prefix by using the given MapsIndoors API Key value
     *
     * @param context           The app's main activity context
     * @param mapsIndoorsAPIKey The current solution's MapsIndoors API Key
     * @since 3.5.0
     */
    public static void setMapsIndoorsAPIKey(@NonNull String mapsIndoorsAPIKey) {
        settingsPrefsBaseFileName = SETTINGS_PREFS_BASE_FILE_NAME + "." + mapsIndoorsAPIKey;
    }

    public static boolean hasUserChosenAVenue(@NonNull Context context) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);
        return sp.getBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_USER_CHOSEN_VENUE, false);
    }

    public static void setUserHasChosenVenue(@NonNull Context context, boolean state) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_USER_CHOSEN_VENUE, state);
        editor.apply();
    }

    public static void setAppVersionName(@NonNull Context context, @NonNull String appVersionName) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        editor.putString(SharedPrefsHelper.SETTINGS_PREFS_KEY_APP_VERSION, appVersionName);
        editor.apply();
    }

    @NonNull
    public static String getAppVersionName(@NonNull Context context) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);

        return sp.getString(SharedPrefsHelper.SETTINGS_PREFS_KEY_APP_VERSION, "");
    }

    public static void setUserTravelingMode(@NonNull Context context, @MapsIndoorsHelper.Vehicle int travelingMode) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SharedPrefsHelper.SETTINGS_PREFS_KEY_USER_CHOSEN_TRAVEL_MODE, travelingMode);
        editor.apply();
    }

    @MapsIndoorsHelper.Vehicle
    public static int getUserTravelingMode(@NonNull Context context) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);

        return sp.getInt(SharedPrefsHelper.SETTINGS_PREFS_KEY_USER_CHOSEN_TRAVEL_MODE, MapsIndoorsHelper.VEHICLE_NONE);
    }

    /**
     * Stores the given venue id
     *
     * @param context
     * @param venueId
     */
    public static void setCurrentVenueId(@NonNull Context context, @NonNull String venueId) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        editor.putString(SharedPrefsHelper.SETTINGS_PREFS_KEY_CURRENT_VENUE_ID, venueId);
        editor.apply();
    }

    /**
     * @param context
     * @return The last venue id set by the user, empty string if nothing has been saved yet
     */
    @NonNull
    public static String getCurrentVenueId(@NonNull Context context) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);

        return sp.getString(SharedPrefsHelper.SETTINGS_PREFS_KEY_CURRENT_VENUE_ID, "");
    }

    public static void setAvoidStairs(@NonNull Context context, boolean avoidStairs) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_AVOID_STAIRS, avoidStairs);
        editor.apply();
    }

    public static boolean getAvoidStairs(@NonNull Context context) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);

        return sp.getBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_AVOID_STAIRS, false);
    }

    public static void setZoomForDetailButtonToShow(@NonNull Context context, boolean buttonToShow) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_SHOW_ZOOM_BUTTON, buttonToShow);
        editor.apply();
    }

    public static boolean isZoomForDetailButtonToShow(@NonNull Context context) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);
        return sp.getBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_SHOW_ZOOM_BUTTON, true);
    }

    public static void setApiKeyValidity(@NonNull Context context, boolean keyValidity) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_API_KEY_VALIDITY, keyValidity);
        editor.apply();
    }

    public static boolean getApiKeyValidity(@NonNull Context context) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);

        return sp.getBoolean(SharedPrefsHelper.SETTINGS_PREFS_KEY_API_KEY_VALIDITY, true);
    }

    /**
     * Note that this is stored in a separate file (not tied to an api key as the others)
     *
     * @param context
     * @param solutionAlias A valid MapsIndoors API Key / solution alias
     */
    public static void setSolutionKeyAlias(@NonNull Context context, @NonNull String solutionAlias) {
        final SharedPreferences sp = context.getSharedPreferences(SETTINGS_PREFS_BASE_FILE_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        editor.putString(SharedPrefsHelper.SETTINGS_PREFS_KEY_SOLUTION_ALIAS_KEY, solutionAlias);
        editor.apply();
    }

    /**
     * Note that this is stored in a separate file (not tied to an api key as the others)
     *
     * @param context
     * @return The last stored MapsIndoors API Key, {@code null} otherwise
     */
    @Nullable
    public static String getSolutionKeyAlias(@NonNull Context context) {
        final SharedPreferences sp = context.getSharedPreferences(SETTINGS_PREFS_BASE_FILE_NAME, Context.MODE_PRIVATE);

        return sp.getString(SharedPrefsHelper.SETTINGS_PREFS_KEY_SOLUTION_ALIAS_KEY, null);
    }

    public static void clearSolutionKeyAlias(@NonNull Context context) {
        final SharedPreferences sp = context.getSharedPreferences(SETTINGS_PREFS_BASE_FILE_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        editor.remove(SharedPrefsHelper.SETTINGS_PREFS_KEY_SOLUTION_ALIAS_KEY);
        editor.apply();
    }

    public static void setSelectedUserRoles(@NonNull Context context, @NonNull Set<String> userRoleIds) {
        final SharedPreferences sp = context.getSharedPreferences( settingsPrefsBaseFileName, Context.MODE_PRIVATE );
        final SharedPreferences.Editor editor = sp.edit();

        editor.remove(SharedPrefsHelper.SETTINGS_PREFS_KEY_SELECTED_USER_ROLES);
        editor.apply();


        final Set<String> values = new HashSet<>( userRoleIds );
        final SharedPreferences.Editor editor1 = context.getSharedPreferences(settingsPrefsBaseFileName, context.MODE_PRIVATE).edit();

        editor1.putStringSet( SharedPrefsHelper.SETTINGS_PREFS_KEY_SELECTED_USER_ROLES, values );
        editor1.apply();
    }

    @NonNull
    public static Set<String> getSelectedUserRoles(@NonNull Context context) {
        final SharedPreferences sp = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);
        final Set<String> values = sp.getStringSet(SharedPrefsHelper.SETTINGS_PREFS_KEY_SELECTED_USER_ROLES, new HashSet<>(0));
        return values;
    }

    public static void clearSharedPref(@NonNull Context context) {
        final SharedPreferences settings = context.getSharedPreferences(settingsPrefsBaseFileName, Context.MODE_PRIVATE);
        settings.edit().clear().apply();
    }
}
