package com.mapsindoors.stdapp.ui.common.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapsindoors.stdapp.R;

public class OverlayFragment extends BaseFragment
{
	@Nullable
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		if( mMainView == null ) {
			mMainView = inflater.inflate( R.layout.fragment_overlay_layout, container );
		}

		return mMainView;
	}

	@Override
	public void connectivityStateChanged( boolean state ) {}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onDrawerEvent( int newState, int prevState ) {}


	public void setAlpha(float alpha){
		mMainView.setAlpha(alpha);
	}
}