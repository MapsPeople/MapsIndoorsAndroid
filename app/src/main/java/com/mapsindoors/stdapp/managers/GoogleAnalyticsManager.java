package com.mapsindoors.stdapp.managers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.dbglog;

import java.util.List;
import java.util.Locale;

/**
 * GoogleAnalyticsManager
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 24/05/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class GoogleAnalyticsManager {
    public static final String TAG = GoogleAnalyticsManager.class.getSimpleName();


    private static final String FIR_GLOBAL = "Global";
    private static final String FIR_SCOPE = "Scope";
    private static final String FIR_QUERY = "Query";
    private static final String FIR_RESULT_COUNT = "Result_Count";


    /**
     * Firebase Analytics
     */
    private static FirebaseAnalytics sFirebaseAnalytics;


    public static synchronized void initialize(Context context) {
        sFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static boolean isTrackingEnabled() {
        return sFirebaseAnalytics != null;
    }

    public static void disableForDebug(boolean dbgDisable) {
        if ((sFirebaseAnalytics != null) && dbgDisable) {
            // This enables/disables Firebase analytics at runtime
            // To fully disable it put it on the manifest:
            // Ref: https://firebase.google.com/support/guides/disable-analytics
            sFirebaseAnalytics.setAnalyticsCollectionEnabled(false);
        }
    }


    //region SCREEN TRACKING

    /**
     * Reports user entering a screen
     *
     * @param name The named screen
     */
    public static void reportScreen(@Size(min = 1L, max = 36L) @NonNull String name, Activity activity) {
        if (isTrackingEnabled()) {
            sFirebaseAnalytics.setCurrentScreen(activity, name, null /* class override */);

            if (dbglog.isDeveloperMode()) {
                DBG_showEvent("reportScreen " + name);
            }
        }
    }
    //endregion


    public static void reportSearch(@Nullable String queryString, @Nullable List<String> categories, long count) {
        if (isTrackingEnabled()) {
            String scope;
            if ((categories != null) && (categories.size() > 0)) {
                scope = TextUtils.join("_", categories.toArray());
            } else {
                scope = FIR_GLOBAL;
            }


            final Bundle bundle = new Bundle();
            bundle.putString(FIR_SCOPE, scope);
            bundle.putString(FIR_QUERY, (queryString != null) ? queryString : "");
            bundle.putLong(FIR_RESULT_COUNT, count);

            sFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);

            {
                StringBuilder sb = new StringBuilder();
                for (String key : bundle.keySet()) {
                    sb.append(key).
                            append(":").
                            append(bundle.get(key)).
                            append(' ');
                }
                if (dbglog.isDeveloperMode()) {
                    DBG_showEvent(String.format(Locale.US, "reportSearch: (%s)", sb.toString().trim()));
                }
            }
        }
    }

    public static void reportSearch(@NonNull String searchString, long count) {
        if (isTrackingEnabled()) {
            final Bundle bundle = new Bundle();
            bundle.putString(FIR_SCOPE, FIR_GLOBAL);
            bundle.putString(FIR_QUERY, searchString);
            bundle.putLong(FIR_RESULT_COUNT, count);

            sFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);

            {
                StringBuilder sb = new StringBuilder();
                for (String key : bundle.keySet()) {
                    sb.append(key).
                            append(":").
                            append(bundle.get(key)).
                            append(' ');
                }
                if (dbglog.isDeveloperMode()) {
                    DBG_showEvent(String.format(Locale.US, "reportSearch: (%s)", sb.toString().trim()));
                }
            }
        }
    }


    public static void reportEvent(@NonNull final String eventName, @Nullable final Bundle bundle) {
        if (isTrackingEnabled()) {
            sFirebaseAnalytics.logEvent(eventName, bundle);

            if (dbglog.isDeveloperMode()) {
                if (bundle != null) {
                    StringBuilder sb = new StringBuilder();
                    for (String key : bundle.keySet()) {
                        sb.append(key).
                                append(":").
                                append(bundle.get(key)).
                                append(' ');
                    }
                }
            }
        }
    }
    //endregion


    private static void DBG_showEvent(@NonNull final String msg) {
        dbglog.Log(TAG, msg);

        final Context ctx = MapsIndoors.getApplicationContext();
        if (ctx != null) {

            if (dbglog.isDeveloperMode()) {
                new Handler(ctx.getMainLooper()).post(() -> Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show());
            }
        }
    }
}
