package com.mapsindoors.stdapp.ui.common.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
	public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
		if( mMainView == null ) {
			mMainView = inflater.inflate( R.layout.fragment_splashscreen, container );
		}
		return mMainView;
	}


	@Override
	public boolean onBackPressed()
	{
		return false;
	}
	//endregion
}
