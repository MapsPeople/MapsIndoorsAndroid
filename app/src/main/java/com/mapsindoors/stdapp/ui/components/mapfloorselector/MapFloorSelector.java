package com.mapsindoors.stdapp.ui.components.mapfloorselector;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.mapsindoors.mapssdk.IFloorSelector;
import com.mapsindoors.mapssdk.OnFloorSelectedListener;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.Building;
import com.mapsindoors.mapssdk.Floor;
import com.mapsindoors.mapssdk.FloorBase;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * MapFloorSelector
 * MapsIndoorsDemo
 *
 * Created by Jose J Varó on 2/28/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class MapFloorSelector extends FrameLayout implements IFloorSelector
{
	public final String TAG = MapFloorSelector.class.getSimpleName();

	private static final int FADE_IN_ANIM_TIME_IN_MS = 500;
	private static final int FADE_OUT_ANIM_TIME_IN_MS = 500;

	public static final int FLAG_PREVENT_SHOW_HIDE_FROM_CONTROL = (1 << 0); // 0001
	public static final int FLAG_DISABLE_AUTO_POPULATE          = (1 << 1); // 0010
	public static final int FLAG_DISABLE_AUTO_FLOOR_CHANGE      = (1 << 2); // 0100


	OnFloorSelectedListener mFloorSelectedListener;
	int mCurrentFloorIndex;
	private ViewPropertyAnimatorCompat mAnimator;


	private MapFloorSelectorAdapter mListAdapter;

	boolean mWillShowView = true;


	/** Set in the populateList methods */
	private boolean mHasFloorsToShow;

	private int mFlags;

	ListView mFloorSelectorListView;


	View bottomScrollGradient;
	View topScrollGradient;

	float currentZoomLevel;


	//region CTOR
	public MapFloorSelector( @NonNull Context context )
	{
		super( context );
		init( context );
	}

	public MapFloorSelector( @NonNull Context context, @Nullable AttributeSet attrs )
	{
		super( context, attrs );
		init( context );
	}

	public MapFloorSelector( @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
		init( context );
	}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	public MapFloorSelector( @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes )
	{
		super( context, attrs, defStyleAttr, defStyleRes );
		init( context );
	}
	//endregion


	private void init( @NonNull Context context)
	{
		inflate( context, R.layout.control_mapsindoors_floor_selector2, this );

		mCurrentFloorIndex = Integer.MAX_VALUE;
		mAnimator = null;
		mFlags = 0;

		bottomScrollGradient = findViewById(R.id.bottom_gradient);
		topScrollGradient = findViewById(R.id.top_gradient);

		mFloorSelectorListView = findViewById( R.id.mapspeople_floor_selector_list );

		mListAdapter = new MapFloorSelectorAdapter( context, R.layout.control_mapsindoors_floor_selector_button );

		mFloorSelectorListView.setAdapter( mListAdapter );

		ViewTreeObserver observer = mFloorSelectorListView.getViewTreeObserver();

		observer.addOnGlobalLayoutListener( () -> {
			if( willMyListScroll() )
			{
				bottomScrollGradient.setVisibility( VISIBLE );
				topScrollGradient.setVisibility( VISIBLE );
			}
			else
			{
				bottomScrollGradient.setVisibility( INVISIBLE );
				topScrollGradient.setVisibility( INVISIBLE );
			}
		});

		mFloorSelectorListView.setOnItemClickListener( ( parent, view, position, id ) -> {

			final Bundle eventParams = new Bundle();
			eventParams.putInt(context.getString( R.string.fir_param_floor_Index), position);
			GoogleAnalyticsManager.reportEvent(context.getString( R.string.fir_event_Map_Floor_Selector_Clicked ), eventParams );

			int newIndex = (int) view.getTag();
			if( newIndex != mCurrentFloorIndex ) {

				setFloorInternal( newIndex );

				if( mFloorSelectedListener != null) {
					mFloorSelectedListener.onFloorSelected( newIndex );
				}
			}
		} );

		show( false, false );
	}

	public void setFlags(int flags)
	{
		mFlags = flags;
	}

	public void addFlags(int flags)
	{
		mFlags |= flags;
	}

	public void clearFlags(int flags)
	{
		mFlags &= ~flags;
	}

	public int getFlags()
	{
		return mFlags;
	}


	//region Implements IFloorSelector
	@Override
	public void setOnFloorSelectedListener( @Nullable OnFloorSelectedListener callback )
	{
		mFloorSelectedListener = callback;
	}

	@Override
	public void populateList( @Nullable Building building ) {

		final int testVar = mFlags & FLAG_PREVENT_SHOW_HIDE_FROM_CONTROL;

		if( building != null && building.getFloors() != null )
		{
			if( BuildConfig.DEBUG )
			{
				if( building.getFloors().isEmpty() )
				{
					dbglog.Log( TAG, "" );
				}
			}

			populateListInternal( building.getFloors() );

			if( testVar == 0 )
			{
				show( true, true );
			}
			mHasFloorsToShow = true;
		}
		else
		{
			//	if( testVar == 0 ) {
			show( false, true );
			//	}
			mHasFloorsToShow = false;
		}
	}

	@Override
	public void onMapZoomLevelChanged( float zoomLevel )
	{
		currentZoomLevel = zoomLevel;

		if( zoomLevel >= (MapsIndoorsSettings.VENUE_TILE_LAYER_VISIBLE_START_ZOOM_LEVEL) )
		{
			//setFlags(0);
			if( mHasFloorsToShow )
			{
				show( true, true );
			}
		}
		else
		{
			//setFlags(1);
			show( false, true );
		}
	}

	@Override
	public void populateList( @Nullable Building building, @Nullable List<Building> buildingList )
	{
		List< FloorBase > floorSelectorEntries = new ArrayList<>();

		if( buildingList != null )
		{
			for( Building b : buildingList )
			{
				for( Floor f : b.getFloors() )
				{
					boolean doAdd = true;
					int cFloorIndex = f.getZIndex();

					for( FloorBase fb : floorSelectorEntries ) {
						if( fb.getZIndex() == cFloorIndex ) {
							doAdd = false;
							break;
						}
					}

					if( doAdd ) {
						floorSelectorEntries.add( f );
					}
				}
			}
		}

		populateListInternal( floorSelectorEntries );

		boolean gotFloors = building!=null;
		if( (mFlags & FLAG_PREVENT_SHOW_HIDE_FROM_CONTROL) == 0 )
		{
			show( gotFloors, true );
		}
	}

	@Override
	public void addToView( @NonNull ViewGroup view ){}

	@Override
	public void setFloor( int floorIndex ) {
		setFloorInternal( floorIndex );
	}

	@Override
	public int getCurrentFloorIndex()
	{
		return (mCurrentFloorIndex == Integer.MAX_VALUE) ? 0 : mCurrentFloorIndex;
	}

	/**
	 * Note: isAutoPopulateEnabled() and isAutoFloorChangeEnabled() are always true now.
	 *       Take them into account if that changes
	 */
	@Override
	public boolean isAutoPopulateEnabled() {
		return (mFlags & FLAG_DISABLE_AUTO_POPULATE) == 0;
	}

	@Override
	public boolean isAutoFloorChangeEnabled() {
		return (mFlags & FLAG_DISABLE_AUTO_FLOOR_CHANGE) == 0;
	}

	/**
	 * @param show      True to show, false to hide
	 * @param animated
	 */
	@Override
	public void show( boolean show, boolean animated )
	{
		// In case the floor selector is already in the desired state, do nothing
		if( mWillShowView == show || (currentZoomLevel < MapsIndoorsSettings.VENUE_TILE_LAYER_VISIBLE_START_ZOOM_LEVEL && show) )
		{
			return;
		}

		if( ((mFlags & FLAG_PREVENT_SHOW_HIDE_FROM_CONTROL) != 0) && show )
		{
			return;
		}

		float cAlpha = this.getAlpha();
		mWillShowView = show;

		if( !animated ) {
			if( show && (cAlpha < 0.1f) ) {
				setAlpha( 1f );
				setVisible( true );
			}
			else if( !show && (cAlpha > 0.9f) ) {
				setAlpha( 0f );
				setVisible( false );
			}
		}
		else {
			mAnimator = (mAnimator != null)
					? mAnimator
					: ViewCompat.animate( this );


			if( show && (cAlpha < 0.1f) ) {
				// Fade in anim setup
				mAnimator.
						alpha( 1f ).
						setDuration( FADE_IN_ANIM_TIME_IN_MS ).
						setListener( mVisAnimatorListener ).
						start();
			}
			else if( !show && (cAlpha > 0.9f) ) {
				// Fade out anim setup"
				mAnimator.
						alpha( 0f ).
						setDuration( FADE_OUT_ANIM_TIME_IN_MS ).
						setListener( mVisAnimatorListener ).
						start();
			}
		}
	}

	@Override
	public boolean isVisible() {
		return !((getAlpha() < 0.1) && (getVisibility() == GONE));
	}
	//endregion


	private void setFloorInternal( int floorIndex )
	{
		if( mCurrentFloorIndex != floorIndex ) {
			mCurrentFloorIndex = floorIndex;
		}

		refreshUI();
	}

	private void populateListInternal( List<?> floors )
	{
		ArrayList< FloorBase > fbList = new ArrayList<>();

		int floorCount = floors.size();
		for( int i = 0; i < floorCount; i++ )
		{
			fbList.add( (FloorBase) floors.get( i ) );
		}

		if( floorCount == 0 )
		{
			return;
		}

		Collections.sort( fbList, FloorBase::compareTo );

		mListAdapter.setList( fbList );

		int fbLowestZIndex = fbList.get( 0 ).getZIndex();
		int fbHighestZIndex = fbList.get( floorCount - 1 ).getZIndex();

		if( getCurrentFloorIndex() < fbLowestZIndex )
		{
			// If the current floor is lower than the lowest existing floor in this new building, we need to select a new floor
			// Selecting the lowest possible floor
			setFloorInternal( fbLowestZIndex );

			if( mFloorSelectedListener != null) {
				mFloorSelectedListener.onFloorSelected( fbLowestZIndex );
			}
		}
		else
		{
			//If the current floor is higher than the highest existing floor in this new building, we need to select a new floor
			if( getCurrentFloorIndex() > fbHighestZIndex )
			{
				//Selecting the lowest possible floor
				setFloorInternal( fbHighestZIndex );
				if( mFloorSelectedListener != null) {
					mFloorSelectedListener.onFloorSelected( fbHighestZIndex );
				}
			}
			else
			{
				refreshUI();
			}
		}
	}


	//region UI
	private void refreshUI() {
//		if( isVisible() ) {
			final int pos = mListAdapter.setSelectedButtonWithFloorValue( getCurrentFloorIndex() );
			mFloorSelectorListView.smoothScrollToPosition( pos );
//		}
	}

	private ViewPropertyAnimatorListener mVisAnimatorListener = new ViewPropertyAnimatorListener() {
		@Override
		public void onAnimationStart( View view ) {
			if( getVisibility() != View.VISIBLE ) {
				setVisible( true );
			}
		}

		@Override
		public void onAnimationEnd( View view ) {
			if( !mWillShowView && (getVisibility() == View.VISIBLE) ) {
				setVisible( false );
			}
		}

		@Override
		public void onAnimationCancel( View view ) {}
	};

	void setVisible( boolean visible ) {
		setVisibility( visible ? View.VISIBLE : View.GONE );
	}

	boolean willMyListScroll()
	{
		if( mFloorSelectorListView.getChildAt( 0 ) != null )
		{
			final int realSizeOfListView = mFloorSelectorListView.getChildAt( 0 ).getHeight() * mListAdapter.getCount();
			final int currentSizeOfListView = mFloorSelectorListView.getHeight();

			return realSizeOfListView > currentSizeOfListView;
		}

		return false;
	}

	//endregion
}
