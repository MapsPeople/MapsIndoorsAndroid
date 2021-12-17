package com.mapsindoors.stdapp.ui.common.fragments;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.View;

import com.mapsindoors.mapssdk.DataSetManagerStatus;
import com.mapsindoors.mapssdk.MPDataSetCache;
import com.mapsindoors.mapssdk.MPDataSetCacheManager;
import com.mapsindoors.mapssdk.MPDataSetCacheSyncListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.enums.DrawerState;
import com.mapsindoors.stdapp.ui.common.enums.MenuFrame;


/**
 * BaseFragment
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 02/03/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public abstract class BaseFragment extends Fragment implements OnStateChangedListener, MPDataSetCacheSyncListener {
    protected View mMainView;
    protected MenuFrame mFragment;
    protected MPDataSetCacheManager mDatasetCacheManager;

    protected BaseFragment() {
        // Setup listener, to handle updated data, e.g. when conneting to network
        mDatasetCacheManager = MPDataSetCacheManager.getInstance();
        mDatasetCacheManager.addMPDataSetCacheSyncListener(this);
    }

    @Override
    public void onDataSetSyncStatusChanged(@NonNull MPDataSetCache dataSetCache, int status) {
        if (status == DataSetManagerStatus.SYNC_FINISHED) {
            onDataUpdated();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatasetCacheManager.removeMPDataSetCacheSyncListener(this);
    }

    public boolean isFragmentSafe() {
        return !isDetached() && !isRemoving() && isActive();
    }

    public boolean isActive() {
        return (mMainView != null) && (mMainView.getVisibility() == View.VISIBLE);
    }

    public boolean isAvailable() {
        return mMainView != null;
    }

    public void connectivityStateChanged(boolean state) {
    }

    public boolean onBackPressed() {
        final Activity activity = getActivity();
        if (activity instanceof MapsIndoorsActivity) {
            MapsIndoorsActivity mapsIndoorsActivity = (MapsIndoorsActivity) activity;
            final boolean imActive = (getActivity() != null) && (mapsIndoorsActivity.getCurrentMenuShown() == mFragment);

            if (imActive) {
                close(mapsIndoorsActivity);
            }

            return true;
        }
        return false;
    }

    public void close(MapsIndoorsActivity activity) {
        activity.menuGoBack();
    }

    public void onDrawerEvent(DrawerState newState, DrawerState prevState) {
    }

    public void willOpen(final MenuFrame fromIndex) {
    }

    public void onDataUpdated() {
    }

    // So the listener can transmit the connectivity changes to all the fragments tha will override "connectivityStateChanged()" method
    @Override
    public void onStateChanged(boolean isEnabled) {
        if (isAdded()) {
            connectivityStateChanged(isEnabled);
        }
    }


}
