package com.mapsindoors.stdapp.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * GPSLocationReceiver
 * MapsIndoorsDemo
 * <p>
 * Created by Mohammed Amine Naimi on 03/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class GPSLocationReceiver extends BroadcastReceiver
{
	public static final String TAG = GPSLocationReceiver.class.getSimpleName();

	// list to store all the listeners
	static List<OnStateChangedListener> sStateChangedListenerList;
	boolean mPreviousState;
	boolean mFirstTime;

	Context mActivityContext;

	public GPSLocationReceiver( Context activityContext )
	{
		if( sStateChangedListenerList == null )
		{
			sStateChangedListenerList = new ArrayList<>();
		}else
		{
			sStateChangedListenerList.clear();
		}

		mActivityContext = activityContext;

		mPreviousState = PSUtils.isLocationServiceEnabled( mActivityContext );

		mFirstTime = true;
	}

	@Override
	public void onReceive( Context context, Intent intent )
	{
		final String intentAction = intent.getAction();

		if( (intentAction != null) && intentAction.matches( LocationManager.PROVIDERS_CHANGED_ACTION ) )
		{
			boolean gpsState = PSUtils.isLocationServiceEnabled( mActivityContext );

			// only when the state changes
			if( mFirstTime )
			{
				reportStateToListeners( gpsState );
				mFirstTime = false;
			}
			else if( gpsState != mPreviousState )
			{
				reportStateToListeners( gpsState );
				mPreviousState = gpsState;
			}
		}
	}

	public static void addOnStateChangedListener( OnStateChangedListener onStateChangedListener )
	{
		if( sStateChangedListenerList != null )
		{
			sStateChangedListenerList.remove( onStateChangedListener );
			sStateChangedListenerList.add( onStateChangedListener );
		}
	}

	public static void removeOnStateChangedListener( OnStateChangedListener onStateChangedListener )
	{
		if( sStateChangedListenerList != null )
		{
			sStateChangedListenerList.remove( onStateChangedListener );
		}
	}

	void reportStateToListeners( boolean state )
	{
		if( (sStateChangedListenerList != null) && !sStateChangedListenerList.isEmpty() )
		{
			for( OnStateChangedListener stChListener : sStateChangedListenerList )
			{
				if( stChListener != null )
				{
					stChListener.onStateChanged( state );
				}
			}
		}
	}

	public void terminate()
	{
		if( sStateChangedListenerList != null )
		{
			sStateChangedListenerList.clear();
			sStateChangedListenerList = null;
		}

		if( mActivityContext != null )
		{
			mActivityContext = null;
		}
	}
}