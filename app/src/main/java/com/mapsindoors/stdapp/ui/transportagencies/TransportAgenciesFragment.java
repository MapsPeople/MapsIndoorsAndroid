package com.mapsindoors.stdapp.ui.transportagencies;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.mapsindoors.mapssdk.AgencyInfo;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.common.enums.MenuFrame;
import com.mapsindoors.stdapp.ui.common.listeners.MenuListener;
import com.mapsindoors.stdapp.ui.transportagencies.adapters.TransportAgenciesAdapter;
import com.mapsindoors.stdapp.ui.transportagencies.models.TransportAgencyItem;

import java.util.ArrayList;
import java.util.List;

/**
 * TransportAgenciesFragment
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 18/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class TransportAgenciesFragment extends BaseFragment {
    private static final String TAG = TransportAgenciesFragment.class.getSimpleName();

    private static final int FLIPPER_LIST_ITEMS = 0;
    private static final int FLIPPER_LIST_PROGRESS = 1;


    private Context mContext;
    private MapsIndoorsActivity mActivity;
    private RecyclerView mTransportAgenciesList;

    private ViewFlipper mViewFlipper;
    private TransportAgenciesAdapter mRecyclerViewAdapter;


    //region Fragment lifecycle events
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transport_sources, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMainView = view;
        mFragment = MenuFrame.MENU_FRAME_TRANSPORT_AGENCIES;
    }
    //endregion


    public void init(Context context) {
        mContext = (mContext != null) ? mContext : context;
        mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) context;

        View backBtn = mMainView.findViewById(R.id.transport_sources_back_button);
        backBtn.setOnClickListener(v -> close(mActivity));

        setupViewFlipper(mMainView);
        setupListView(mMainView);
    }

    //region List
    private void setupViewFlipper(View view) {
        mViewFlipper = view.findViewById(R.id.transport_sources_viewflipper);
        mViewFlipper.setDisplayedChild(FLIPPER_LIST_PROGRESS);
    }

    private void setupListView(View view) {
        mContext = (mContext != null) ? mContext : getContext();
        mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) mContext;

        mTransportAgenciesList = view.findViewById(R.id.transport_sources_list);
        mTransportAgenciesList.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setSmoothScrollbarEnabled(false);
        layoutManager.setAutoMeasureEnabled(true);
        mTransportAgenciesList.setLayoutManager(layoutManager);

        mRecyclerViewAdapter = new TransportAgenciesAdapter(mContext); //mMenuListener );
        mTransportAgenciesList.setAdapter(mRecyclerViewAdapter);
    }

    /**
     * @param transportAgenciesList
     */
    public void setList(@NonNull List<AgencyInfo> transportAgenciesList) {
        List<TransportAgencyItem> taItems = new ArrayList<>(transportAgenciesList.size());

        for (AgencyInfo agencyInfo : transportAgenciesList) {
            taItems.add(new TransportAgencyItem(agencyInfo.getName(), agencyInfo.getUrl()));
        }

        mRecyclerViewAdapter.setItems(taItems);
        updateViewFlipper();
    }

    private void updateViewFlipper() {
        if (mViewFlipper.getDisplayedChild() != FLIPPER_LIST_ITEMS) {
            mViewFlipper.setDisplayedChild(FLIPPER_LIST_ITEMS);
        }
    }
    //endregion
}
