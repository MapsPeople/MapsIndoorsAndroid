package com.mapsindoors.stdapp.ui.appInfo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.appInfo.adapters.AppInfoAdapter;
import com.mapsindoors.stdapp.ui.appInfo.models.CreditItem;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.direction.DirectionsVerticalFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * AppInfoFragment
 * MapsIndoorsDemo
 * <p>
 * Created by Amine on 15/11/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class AppInfoFragment extends BaseFragment
{
	private static final String TAG = AppInfoFragment.class.getSimpleName();

	private static final int FLIPPER_LIST_ITEMS             = 0;

	private List<CreditItem> mCreditList;
	private Context mContext;
	private MapsIndoorsActivity         mActivity;
	private RecyclerView mCreditsListRecyclerView;
	private ViewFlipper                 mViewFlipper;
	private AppInfoAdapter mRecyclerViewAdapter;

	TextView providerTextView;
	TextView appVersiontextView;
	TextView sdkVersionTextview;
	View mapsPeopleASLayout;
	View mFeedbackLayout;
	//region Fragment lifecycle events


	AppConfigManager appConfigManager ;


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
		return inflater.inflate( R.layout.fragment_app_info, container );
	}

	@Override
	public void onViewCreated( View view, @Nullable Bundle savedInstanceState )
	{
		super.onViewCreated(view, savedInstanceState);
		mMainView = view;

		providerTextView = view.findViewById(R.id.app_info_provider_name);
		appVersiontextView = view.findViewById(R.id.app_info_app_version);
		sdkVersionTextview = view.findViewById(R.id.app_info_sdk_version);
		mapsPeopleASLayout = view.findViewById(R.id.app_info_maps_people_as_layout);
		mFeedbackLayout = view.findViewById(R.id.feedback_layout);

		if( BuildConfig.DEBUG_ENABLE_UI_SETTINGS )
		{
			View debugSettings = view.findViewById( R.id.debug_settings );

			if( debugSettings != null )
			{
				debugSettings.setVisibility( View.VISIBLE );

				{
					android.widget.Switch switchView = debugSettings.findViewById( R.id.debug_control_route_offline_switch );
					switchView.setOnCheckedChangeListener( ( compoundButton, enable ) -> {

						DirectionsVerticalFragment dirFragment = mActivity.getVerticalDirectionsFragment();
						if(dirFragment !=null)
						{
							//dirFragment.DBG_EnableDebugUI( enable );
						}
					});
				}
			}
		}
	}
	//endregion


	public void init( Context context)
	{
		mContext  =  (mContext!=null) ? mContext : context;
		mActivity =  (mActivity!=null) ? mActivity : (MapsIndoorsActivity)context;

		appConfigManager =  mActivity.getAppConfigManager();

		//
		sdkVersionTextview.setText( MapsIndoors.getSDKVersion() );
		//
		appVersiontextView.setText( getAppVersion() );

		//
		View backBtn = mMainView.findViewById(R.id.app_info_back_button );
		backBtn.setOnClickListener( v -> close() );

		mapsPeopleASLayout.setOnClickListener( view -> {
			Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( getResources().getString(R.string.app_supplier_website) ) );
			mContext.startActivity( browserIntent );
		} );


		if( appConfigManager != null )
		{
			final String feedbackURL = appConfigManager.getFeedbackUrl();

			if (!TextUtils.isEmpty(feedbackURL))
			{
				try {
					Uri feedbackURI = Uri.parse(feedbackURL);
					//
					mFeedbackLayout.setOnClickListener( view -> {

						Intent intent = new Intent(Intent.ACTION_VIEW, feedbackURI);

						if (intent != null) {
							try{
								mContext.startActivity(intent);
							}catch(Exception ex){
								if( BuildConfig.DEBUG ){
									Toast.makeText(mContext, TAG +" - Parsing the feedback URI Error, check the log!!!", Toast.LENGTH_SHORT).show();
									dbglog.LogE( TAG, "Parsing the feedback URI Error:\n" + ex.toString() );
								}
							}
						}
					} );
					//
					mFeedbackLayout.setVisibility(View.VISIBLE);

				} catch (Exception ex) {

					if( BuildConfig.DEBUG ){
						Toast.makeText(mContext, TAG +" - Parsing the feedback URI Error, check the log!!!", Toast.LENGTH_SHORT).show();
						dbglog.LogE( TAG, "Parsing the feedback URI Error:\n" + ex.toString() );
					}
				}
			}
		}

		//
		providerTextView.setOnClickListener( view -> {
			Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( getResources().getString(R.string.app_provider_website) ) );
			mContext.startActivity( browserIntent );
		} );

		//
		setupViewFlipper( mMainView );
		setupListView( mMainView );
	}

	public void close() {
		mActivity.menuGoTo( MapsIndoorsActivity.MENU_FRAME_MAIN_MENU, true );
	}


	//region List
	private void setupViewFlipper( View view )
	{
		mViewFlipper = view.findViewById( R.id.transport_sources_viewflipper );
	}

	private void setupListView( View view )
	{
		mContext = (mContext != null) ? mContext : getContext();
		mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) mContext;
		mCreditsListRecyclerView = view.findViewById( R.id.credit_list);
		mCreditsListRecyclerView.setHasFixedSize( true );

		LinearLayoutManager layoutManager = new LinearLayoutManager( mContext );
		layoutManager.setSmoothScrollbarEnabled( false );
		layoutManager.setAutoMeasureEnabled( true );
		mCreditsListRecyclerView.setLayoutManager( layoutManager );

		initCreditList();
		mRecyclerViewAdapter = new AppInfoAdapter( mContext,mCreditList );
		mCreditsListRecyclerView.setAdapter( mRecyclerViewAdapter );
	}

	/**
	 *
	 * @param AppInfoCreditList
	 */
	public void setList( @NonNull List<CreditItem> AppInfoCreditList )
	{
		List<CreditItem> taItems = new ArrayList<>( AppInfoCreditList.size() );

		for( CreditItem credit : AppInfoCreditList ) {
			taItems.add( new CreditItem( credit.name, credit.url ) );
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
	public void connectivityStateChanged(boolean state) {}

	//region Implements IActivityEvents
	@Override
	public boolean onBackPressed() {
		final boolean imActive = (mActivity != null) && (mActivity.getCurrentMenuShown() == MapsIndoorsActivity.MENU_FRAME_APP_INFO);
		if( imActive ) {
			close();
			return false;
		}

		return true;
	}

	@Override
	public void onDrawerEvent( int newState, int prevState ){}
	//endregion


	String getAppVersion(){
		return BuildConfig.VERSION_NAME;
	}

	void initCreditList()
	{
		String[] libraries = getResources().getStringArray( R.array.credits );
		String[] licences = getResources().getStringArray( R.array.licence );

		int libCount = libraries.length;
		mCreditList = new ArrayList<>(libCount);

		for( int i = 0; i < libCount; i++ )
		{
			mCreditList.add( new CreditItem( libraries[i], licences[i] ) );
		}
	}
}
