package com.mapsindoors.stdapp.positionprovider.gpsPositionProvider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapsindoors.mapssdk.OnPositionUpdateListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;

/**
 * GoogleAPIPositionProviderV2
 * MISDKAND
 * <p>
 * Created by Jose J Varo on 14-11-2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class GoogleAPIPositionProviderV2 implements PositionProvider
{
	// *******************************************************************************************
	//
	// Check
	//  -   https://developers.googleblog.com/2017/11/migrating-to-new-play-games-services.html
	//  -   https://android-developers.googleblog.com/2017/06/reduce-friction-with-new-location-apis.html
	//
	// *******************************************************************************************


	//region IMPLEMENTS PositionProvider



	@NonNull
	@Override
	public String[] getRequiredPermissions()
	{
		return new String[0];
	}

	@Override
	public boolean isPSEnabled() {
		return false;
	}

	@Override
	public void startPositioning( @Nullable String arg )
	{

	}

	@Override
	public void stopPositioning( @Nullable String arg )
	{

	}

	@Override
	public boolean isRunning()
	{
		return false;
	}

	@Override
	public void addOnPositionUpdateListener( @Nullable OnPositionUpdateListener listener )
	{

	}

	@Override
	public void removeOnPositionUpdateListener( @Nullable OnPositionUpdateListener listener )
	{

	}

	@Override
	public void setProviderId( @Nullable String id )
	{

	}

	@Override
	public void addOnstateChangedListener(OnStateChangedListener onStateChangedListener) {
	}

	@Override
	public void removeOnstateChangedListener(OnStateChangedListener onStateChangedListener) {
	}

	@Override
	public void checkPermissionsAndPSEnabled(PermissionsAndPSListener permissionAPSlist) {

	}

	@Nullable
	@Override
	public String getProviderId()
	{
		return null;
	}

	@Nullable
	@Override
	public PositionResult getLatestPosition()
	{
		return null;
	}

	@Override
	public void startPositioningAfter( int delayInMs, @Nullable String arg )
	{

	}

	@Override
	public void terminate()
	{

	}
	//endregion
}
