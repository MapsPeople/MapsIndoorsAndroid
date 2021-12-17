package com.mapsindoors.stdapp;

import android.content.res.Configuration;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.FirebaseApp;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.helpers.AppDevSettings;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.squareup.picasso.Picasso;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;


/**
 * Application
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 17/Jan/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class Application extends android.app.Application {
    public static final String TAG = Application.class.getSimpleName();


    private static Application sInstance;
    public long mAppCreateTime;

    @Override
    public void onCreate() {
        sInstance = this;
        mAppCreateTime = System.nanoTime();

        super.onCreate();

        // Enable/disable internal debugging ASAP and add a tag prefix to the MI SDK logs
        dbglog.enableDeveloperMode(AppDevSettings.ENABLE_MI_DEV_DEBUG, BuildConfig.FLAVOR + "_");

        //
        enableOSStrictMode();
        enablePicassoDebugInfo();

        // ------------------------------------
        // Initialize MapsIndoors
        // First, set the application context, the (initial?) MapsIndoors API Key
        // This just sets the id on an internal static var and (?)...
        MapsIndoors.initialize(
                getApplicationContext(),
                getString(R.string.mapsindoors_api_key)
        );

        MapsIndoors.enableOfflineTilesUpdates(BuildConfig.ENABLE_OFFLINE_TILES_AUTO_FETCH);

        // Google API Key (used here for routing, etc.)
        MapsIndoors.setGoogleAPIKey(getString(R.string.google_maps_key));

        //
        initCustomFont();
        initWhateverLytics();
    }

    @NonNull
    public static Application getInstance() {
        return sInstance;
    }

    private void initCustomFont() {
        String fontPath = getString(R.string.font_roboto_default);

        if (fontPath.isEmpty()) {
            return;
        }

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath(fontPath)
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
        /*
        final CalligraphyConfig config = new CalligraphyConfig.Builder()
                .setDefaultFontPath(fontPath)
                .setFontAttrId(R.attr.fontPath)
                .build();

        CalligraphyConfig.initDefault(config);

         */
    }


    /**
     * Crashlytics and alike go here...
     */
    private void initWhateverLytics() {
        // ================================================================
        // Crashlytics
        /* Crashlytics crashlyticsKit = new Crashlytics.Builder().
                core(new CrashlyticsCore.Builder().
                        build()).
                build();

		Fabric.with(this, crashlyticsKit );
		*/
		// ================================================================

        FirebaseApp.initializeApp(this);
		GoogleAnalyticsManager.initialize( this );

        // When dry run is set, hits will not be dispatched, but will still be logged as
        // though they were dispatched.
        if (!AppDevSettings.ENABLE_ANALYTICS) {
            GoogleAnalyticsManager.disableForDebug(true);
        }

        // ================================================================
        // Other lytics-ish go here
    }

    private void enableOSStrictMode() {
        if (AppDevSettings.ENABLE_OS_STRICTMODE) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().
                            detectAll().
                            penaltyLog().
                            penaltyFlashScreen().
                            build()
            );
            StrictMode.setVmPolicy(
                    new StrictMode.VmPolicy.Builder().
                            detectAll().
                            penaltyLog().
                            build()
            );
        }
    }

    private void enablePicassoDebugInfo() {
        if (AppDevSettings.DEBUG_PICASSO_ENABLED) {
            // Check: "Debug indicators" in http://square.github.io/picasso/
            Picasso.get().setIndicatorsEnabled(true);
            Picasso.get().setLoggingEnabled(true);
        }
    }


    //region Application events
    @Override
    public void onConfigurationChanged(@Nullable Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        MapsIndoors.onApplicationConfigurationChanged(newConfig);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

    }
    //endregion
}
