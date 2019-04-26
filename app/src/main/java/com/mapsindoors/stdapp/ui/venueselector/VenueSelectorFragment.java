package com.mapsindoors.stdapp.ui.venueselector;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.Venue;
import com.mapsindoors.mapssdk.VenueCollection;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.SharedPrefsHelper;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.common.listeners.MenuListener;
import com.mapsindoors.stdapp.ui.venueselector.adapters.VenueSelectorAdapter;
import com.mapsindoors.stdapp.ui.venueselector.listeners.IVenueClickedListener;
import com.mapsindoors.stdapp.ui.venueselector.models.VenueSelectorItem;

import java.util.ArrayList;
import java.util.List;

/**
 * VenueSelectorFragment
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 11/04/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class VenueSelectorFragment extends BaseFragment
        implements
            IVenueClickedListener
{

    private static final String TAG = VenueSelectorFragment.class.getSimpleName();

    private static final int FLIPPER_LIST_ITEMS = 0;
    private static final int FLIPPER_LIST_PROGRESS = 1;


    private Context mContext;
    private MapsIndoorsActivity mActivity;
    private MenuListener mMenuListener;

    private RecyclerView mVenueSelectorList;

    private ViewFlipper mViewFlipper;
    private VenueSelectorAdapter mRecyclerViewAdapter;

    View venueSelectorBackBtn;
    View venueSelectorBtn;


    //region Fragment lifecycle events

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_venue_selector, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMainView = view;
    }
    //endregion


    public void init(Context context, MenuListener menuListener, MapControl mapControl, VenueCollection venues)
    {
        mContext = (mContext != null) ? mContext : context;
        mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) context;
        mMenuListener = menuListener;

        venueSelectorBtn = mMainView.findViewById(R.id.venueselector_venue_ic);
        venueSelectorBackBtn = mMainView.findViewById(R.id.venueselector_back_button);

        venueSelectorBackBtn.setOnClickListener( v -> close() );

        setupViewFlipper(mMainView);
        setupListView(mMainView);
    }

    public void close() {
        mActivity.menuGoBack();
    }


    //region List
    private void setupViewFlipper(View view) {
        mViewFlipper = view.findViewById(R.id.venue_selector_viewflipper);
        mViewFlipper.setDisplayedChild(FLIPPER_LIST_PROGRESS);
    }

    private void setupListView(View view) {
        mContext = (mContext != null) ? mContext : getContext();
        mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) mContext;

        mVenueSelectorList = view.findViewById(R.id.venue_selector_list);
        mVenueSelectorList.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setSmoothScrollbarEnabled(false);
        layoutManager.setAutoMeasureEnabled(true);
        mVenueSelectorList.setLayoutManager(layoutManager);

        mRecyclerViewAdapter = new VenueSelectorAdapter( this);
        mVenueSelectorList.setAdapter(mRecyclerViewAdapter);
    }

    public void onDataReady() {

        AppConfigManager appConfigManager = mActivity.getAppConfigManager();
        VenueCollection venueCollection = mActivity.getVenueCollection();

        if ((venueCollection != null) && (appConfigManager != null))
        {
            List<Venue> venues = venueCollection.getVenues();

            List<VenueSelectorItem> venueItemList = new ArrayList<>(venues.size());

            for (Venue venue : venues) {

                VenueSelectorItem venueItem = new VenueSelectorItem(venue.getVenueId(), venue.getVenueInfo().getName(), appConfigManager.getVenueImage(venue.getName()));
                venueItemList.add(venueItem);
            }

            mRecyclerViewAdapter.setItems(venueItemList);
        }

        updateViewFlipper();
    }

    private void updateViewFlipper() {
        if (mViewFlipper.getDisplayedChild() != FLIPPER_LIST_ITEMS) {
            mViewFlipper.setDisplayedChild(FLIPPER_LIST_ITEMS);
        }
    }
    //endregion


    @Override
    public void connectivityStateChanged( boolean state ) {}

    //region Implements IActivityEvents
    @Override
    public boolean onBackPressed() {
        final boolean imActive = (mActivity != null) && (mActivity.getCurrentMenuShown() == MapsIndoorsActivity.MENU_FRAME_VENUE_SELECTOR);
        if (imActive) {
            close();
            return false;
        }

        return true;
    }

    @Override
    public void onDrawerEvent(int newState, int prevState) {}
    //endregion


    //region Implements IVenueClickedListener
    @Override
    public void OnVenueClicked( final String venueId )
    {
        if( mMenuListener != null )
        {
            mMenuListener.onMenuVenueSelect( venueId );

            if( mContext != null )
            {
                new Handler( mContext.getMainLooper() ).postDelayed( () -> {
                    //close();
                    mActivity.menuGoTo( MapsIndoorsActivity.MENU_FRAME_MAIN_MENU, true );
                    setToolbarBackAndVenueButtonVisibility( true );

                    SharedPrefsHelper.setUserHasChoosenVenue( mContext, true );

                    {
                        final Bundle eventParams = new Bundle();
                        eventParams.putString( getString( R.string.fir_param_Venue ), venueId );

                        GoogleAnalyticsManager.reportEvent( getString( R.string.fir_event_Venue_Selected ), eventParams );
                    }
                }, 150 );
            }
        }
    }
    //endregion


    public void setToolbarBackAndVenueButtonVisibility( boolean backButtonState )
    {
        if (backButtonState) {
            venueSelectorBackBtn.setVisibility(View.VISIBLE);
            venueSelectorBtn.setVisibility(View.GONE);
        } else {
            venueSelectorBackBtn.setVisibility(View.GONE);
            venueSelectorBtn.setVisibility(View.VISIBLE);
        }
    }
}
