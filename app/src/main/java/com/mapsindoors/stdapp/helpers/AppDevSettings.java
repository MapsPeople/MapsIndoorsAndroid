package com.mapsindoors.stdapp.helpers;

import com.mapsindoors.stdapp.BuildConfig;

/**
 * AppDevSettings
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 8/22/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class AppDevSettings
{

	public static final boolean ENABLE_MI_DEV_DEBUG = BuildConfig.DEBUG;

	public static final boolean ENABLE_ANALYTICS = !BuildConfig.DEBUG;

	public static final boolean ENABLE_LEAKCANARY = BuildConfig.DEBUG;

	public static final boolean ENABLE_OS_STRICTMODE = false;



	public static final boolean DEBUG_PICASSO_ENABLED = false;
}
