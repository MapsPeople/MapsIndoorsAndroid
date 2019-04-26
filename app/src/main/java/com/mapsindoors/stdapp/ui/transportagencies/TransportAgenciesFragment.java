package com.mapsindoors.stdapp.ui.transportagencies;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.AgencyInfo;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
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
public class TransportAgenciesFragment extends BaseFragment
{
	private static final String TAG = TransportAgenciesFragment.class.getSimpleName();

	private static final int FLIPPER_LIST_ITEMS             = 0;
	private static final int FLIPPER_LIST_PROGRESS          = 1;


	private Context mContext;
	private MapsIndoorsActivity         mActivity;
	private RecyclerView                mTransportAgenciesList;

	private ViewFlipper                 mViewFlipper;
	private TransportAgenciesAdapter    mRecyclerViewAdapter;


	//region Fragment lifecycle events

	@Override
	public void onAttach(Context context)
	{
		super.onAttach( context );
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		return inflater.inflate( R.layout.fragment_transport_sources, container );
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		mMainView = view;
	}
	//endregion


	public void init( Context context, MenuListener menuListener, MapControl mapControl)
	{
		mContext  =  (mContext!=null) ? mContext : context;
		mActivity =  (mActivity!=null) ? mActivity : (MapsIndoorsActivity)context;

		View backBtn = mMainView.findViewById(R.id.transport_sources_back_button );
		backBtn.setOnClickListener( v -> close() );

		setupViewFlipper( mMainView );
		setupListView( mMainView );
	}

	public void close() {
		mActivity.menuGoBack();
	}


	//region List
	private void setupViewFlipper( View view )
	{
		mViewFlipper = view.findViewById( R.id.transport_sources_viewflipper );
		mViewFlipper.setDisplayedChild( FLIPPER_LIST_PROGRESS );
	}

	private void setupListView( View view )
	{
		mContext = (mContext != null) ? mContext : getContext();
		mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) mContext;

		mTransportAgenciesList = view.findViewById( R.id.transport_sources_list );
		mTransportAgenciesList.setHasFixedSize( true );

		LinearLayoutManager layoutManager = new LinearLayoutManager( mContext );
		layoutManager.setSmoothScrollbarEnabled( false );
		layoutManager.setAutoMeasureEnabled( true );
		mTransportAgenciesList.setLayoutManager( layoutManager );

		mRecyclerViewAdapter = new TransportAgenciesAdapter( mContext ); //mMenuListener );
		mTransportAgenciesList.setAdapter( mRecyclerViewAdapter );
	}

	/**
	 *
	 * @param transportAgenciesList
	 */
	public void setList( @NonNull List<AgencyInfo > transportAgenciesList )
	{
		List< TransportAgencyItem > taItems = new ArrayList<>( transportAgenciesList.size() );

		for( AgencyInfo agencyInfo : transportAgenciesList ) {
			taItems.add( new TransportAgencyItem( agencyInfo.getName(), agencyInfo.getUrl() ) );
		}

		mRecyclerViewAdapter.setItems( taItems );
		updateViewFlipper();
	}

	private void updateViewFlipper() {
		if( mViewFlipper.getDisplayedChild() != FLIPPER_LIST_ITEMS ) {
			mViewFlipper.setDisplayedChild( FLIPPER_LIST_ITEMS );
		}
	}
	//endregion


	@Override
	public void connectivityStateChanged( boolean state ) {}

	//region Implements IActivityEvents
	@Override
	public boolean onBackPressed() {
		final boolean imActive = (mActivity != null) && (mActivity.getCurrentMenuShown() == MapsIndoorsActivity.MENU_FRAME_TRANSPORT_AGENCIES);
		if( imActive ) {
			close();
			return false;
		}

		return true;
	}

	@Override
	public void onDrawerEvent( int newState, int prevState ){}
	//endregion
}
