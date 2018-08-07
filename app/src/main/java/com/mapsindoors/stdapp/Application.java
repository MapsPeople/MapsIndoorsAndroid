package com.mapsindoors.stdapp;

import android.content.res.Configuration;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.helpers.AppDevSettings;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;



/**
 * Application
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 17/Jan/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class Application extends android.app.Application 
{
	public static final String TAG = Application.class.getSimpleName();


    private static Application sInstance;
	public long mAppCreateTime;



	@Override
    public void onCreate()
    {
        sInstance = this;
        mAppCreateTime = System.nanoTime();

        super.onCreate();

	    {
		    // Enable/disable internal debugging ASAP
		    dbglog.useDebug( AppDevSettings.ENABLE_MI_DEV_DEBUG  ); //AppDevSettings.ENABLE_MI_DEV_DEBUG );

		    // Add a log tag prefix to the MI SDK logs
		    dbglog.setCustomTagPrefix( BuildConfig.FLAVOR + "_" );
	    }

		//
	    enableOSStrictMode();
	    enableLeakCanary();
	    enablePicassoDebugInfo();

	    // ------------------------------------
	    // Initialize MapsIndoors
	    // First, set the application context, the (initial?) MapsIndoors API Key
	    // This just sets the id on an internal static var and (?)...
		MapsIndoors.initialize(
				getApplicationContext(),
				getString( R.string.mapsindoors_api_key )
		);

	    // Google API Key (used here for routing, etc.)
		MapsIndoors.setGoogleAPIKey( getString( R.string.google_maps_key ) );

	    //
        initCustomFont();
        initPermissionsHandler();
        initWhateverLytics();
    }

    @NonNull
	public static Application getInstance() {
		return sInstance;
	}

	private void initCustomFont()
    {
        String fontPath = getString(R.string.font_roboto_default);

        if (fontPath.isEmpty()) {
            return;
        }

	    final CalligraphyConfig config = new CalligraphyConfig.Builder()
			    .setDefaultFontPath( fontPath )
			    .setFontAttrId( R.attr.fontPath )
			    .build();

	    CalligraphyConfig.initDefault( config );
    }

    private void initPermissionsHandler()
    {
    }

	/**
	 * Crashlytics and alike go here...
	 */
	private void initWhateverLytics()
	{
		// ================================================================
		// Crashlytics
		Crashlytics crashlyticsKit = new Crashlytics.Builder().
				core( new CrashlyticsCore.Builder().
						disabled( BuildConfig.DEBUG ).
						build() ).
				build();

		Fabric.with(this, crashlyticsKit );

		// ================================================================
		GoogleAnalyticsManager.initialize( this );

		// When dry run is set, hits will not be dispatched, but will still be logged as
		// though they were dispatched.
		if( !AppDevSettings.ENABLE_ANALYTICS ) {
			GoogleAnalyticsManager.disableForDebug( true );
		}

		// ================================================================
		// Other lytics-ish go here
	}

	private void enableLeakCanary()
	{
		if(AppDevSettings.ENABLE_LEAKCANARY)
		{
			// Leak canary
			if( LeakCanary.isInAnalyzerProcess( this ) ) {
				return;
			}
			LeakCanary.install( this );
		}
	}

	private void enableOSStrictMode()
	{
		if( AppDevSettings.ENABLE_OS_STRICTMODE )
		{
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

	private void enablePicassoDebugInfo()
	{
		if( AppDevSettings.DEBUG_PICASSO_ENABLED)
		{
			// Check: "Debug indicators" in http://square.github.io/picasso/
			Picasso.get().setIndicatorsEnabled( true );
			Picasso.get().setLoggingEnabled( true );
		}
	}


	//region Application events
	@Override
	public void onConfigurationChanged( @Nullable Configuration newConfig )
	{
		super.onConfigurationChanged( newConfig );

		MapsIndoors.onApplicationConfigurationChanged( newConfig );
	}

	@Override
	public void onTrimMemory( int level )
	{
		super.onTrimMemory( level );

	}

	//endregion
}
