package com.mapsindoors.stdapp.ui.common.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapsindoors.stdapp.R;


/**
 * SplashScreenFragment
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 19/03/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class SplashScreenFragment extends BaseFragment
{
	@Nullable
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		if( mMainView == null ) {
			mMainView = inflater.inflate( R.layout.fragment_splashscreen, container );
		}
		return mMainView;
	}

	@Override
	public void connectivityStateChanged(boolean state) {}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onDrawerEvent( int newState, int prevState ) {}
}
