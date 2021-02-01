/*
 * Copyright © 2018 MapsPeople A/S. All rights reserved.
 */
package com.mapsindoors.stdapp.ui.components.noInternetBar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.stdapp.R;

/**
 *
 *
 * @author Mohammed Amine
 */

public class NoInternetBar extends FrameLayout
{

	public static final int MESSAGE_STATE = 0;
	public static final int REFRESHING_STATE = 1;


	View    mRootView;
	Context mContext;
	int     mState;

	ImageView logo;
	TextView  label;


	//region CTOR
	public NoInternetBar( @NonNull Context context )
	{
		super( context );
		init( context );
	}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	public NoInternetBar( @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes )
	{
		super( context, attrs, defStyleAttr, defStyleRes );
		init( context );
	}

	public NoInternetBar( @NonNull Context context, @Nullable AttributeSet attrs )
	{
		super( context, attrs );
		init( context );
	}

	public NoInternetBar( @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
		init( context );
	}
	//endregion


	void init( Context context )
	{
		mRootView = inflate( context, R.layout.control_no_internet_message, this );
		mContext = context;
		setupView( mRootView );
		mState = 0;
	}

	void setupView( View view )
	{
		view.setOnClickListener( view1 -> {

			Intent gpsOptionsIntent;

			if( isAirplaneModeOn( mContext ) )
			{
				gpsOptionsIntent = new Intent( Settings.ACTION_AIRPLANE_MODE_SETTINGS );
			}
			else
			{
				gpsOptionsIntent = new Intent( Settings.ACTION_WIRELESS_SETTINGS );
			}

			mContext.startActivity( gpsOptionsIntent );
		});

		label = view.findViewById( R.id.no_internet_text );
		logo = view.findViewById( R.id.no_internet_logo );

	}

	static boolean isAirplaneModeOn( Context context )
	{
		return Settings.System.getInt( context.getContentResolver(), android.provider.Settings.Global.AIRPLANE_MODE_ON, 0 ) != 0;
	}

	public void setState( int newState )
	{
		if( newState != mState )
		{
			Context ctx = MapsIndoors.getApplicationContext();
			if( ctx == null )
			{
				return;
			}

			Resources.Theme appTheme = ctx.getTheme();
			Resources res = ctx.getResources();

			switch( newState )
			{
				case REFRESHING_STATE:
					logo.setImageResource( R.drawable.ic_refresh_24dp );
					label.setText( res.getText( R.string.no_internet_trying_message ) );
					label.setTextColor( ResourcesCompat.getColor( res, R.color.black, appTheme) );
					break;

				case MESSAGE_STATE:
				{
					logo.setImageResource( R.drawable.ic_cloud_off_black_24dp );
					label.setText( res.getText( R.string.no_internet_reconnect_message ) );
					label.setTextColor( ResourcesCompat.getColor( res, R.color.blueGray, appTheme) );
					break;
				}
			}

			mState = newState;
		}
	}
}
