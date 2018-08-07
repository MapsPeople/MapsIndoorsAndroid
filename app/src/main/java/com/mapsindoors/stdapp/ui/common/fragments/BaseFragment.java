package com.mapsindoors.stdapp.ui.common.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import com.mapsindoors.mapssdk.OnStateChangedListener;


/**
 * BaseFragment
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 02/03/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public abstract class BaseFragment extends Fragment implements OnStateChangedListener
{
	protected View mMainView;

	public boolean isFragmentSafe() {
		return !isDetached() && !isRemoving() && isActive();
	}

	public boolean isActive()
	{
		return (mMainView != null) && (mMainView.getVisibility() == View.VISIBLE);
	}

	public abstract void connectivityStateChanged( boolean state );

	public abstract boolean onBackPressed();

	public abstract void onDrawerEvent( int newState, int prevState );


	// so the listener can transmet the connectivity changments to all the fragments tha will override "connectivityStateChanged()" method
	@Override
	public void onStateChanged(boolean isEnabled) {

		if (isAdded()) {
			connectivityStateChanged( isEnabled);
		}
	}
}
