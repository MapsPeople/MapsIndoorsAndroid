package com.mapsindoors.stdapp.managers;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.Venue;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.activitymain.TopSearchField;

import java.util.List;
import java.util.Locale;


/**
 * Created by jose on 07-05-2017.
 *
 * Temp class for handling location selection
 */
public class SelectionManager
{

	public static final int QUERY_CATEGORY        = (1 << 0);
	public static final int QUERY_TYPE            = (1 << 1);

	private static final int LAST_SELECTION_TYPE_VENUE             = 0;
	private static final int LAST_SELECTION_TYPE_SINGLE_LOCATION   = 1;
	private static final int LAST_SELECTION_TYPE_NEAREST_POSITION  = 2;
	private static final int LAST_SELECTION_TYPE_SEARCH_RESULT     = 3;


	private MapsIndoorsActivity mActivity;
	private TopSearchField mTopSearchField;

	private Venue mCurrentVenue;

	/** Category name (facilities, etc.), Type (POI */
	private String mSelectedLocationCriteria;
	private MapControl mMapControl;

	//
	Object currentSelection;

	private int lastSelectionType;
	private int lastCriteriaFlags;


	public SelectionManager( MapsIndoorsActivity activity, MapControl mapControl, TopSearchField topSearchField )
	{
		mActivity = activity;
		mTopSearchField = topSearchField;
		mMapControl = mapControl;
	}

	public void clearSelection()
	{
		mTopSearchField.setToolbarText( null, false );
		lastSelectionType = -1;

		currentSelection = null;

		if( mMapControl != null )
		{
			mMapControl.deSelectLocation();
		}
	}

	public void setCurrentVenue( @Nullable Venue venue )
	{
		if( venue != null )
		{
			mCurrentVenue = venue;

			mMapControl.selectFloor( mCurrentVenue.getDefaultFloor() );


			final GoogleMap gmap = mActivity.getGoogleMap();
			final Point cameraTargetPoint = mCurrentVenue.getAnchor();

			if( gmap != null )
			{
				final CameraPosition newCameraPosition = new CameraPosition.Builder().
						target( cameraTargetPoint.getLatLng() ).
						zoom( MapsIndoorsSettings.VENUE_TILE_LAYER_VISIBLE_START_ZOOM_LEVEL ).
						tilt( 0 ).
						bearing( 0 ).
						build();

				gmap.moveCamera( CameraUpdateFactory.newCameraPosition( newCameraPosition ) );
			}

			mTopSearchField.setToolbarText( venue.getVenueInfo().getName(), false );

			currentSelection = mCurrentVenue;
			lastSelectionType = LAST_SELECTION_TYPE_VENUE;
		}
	}

	public void selectLocation( Location location, boolean changeZoomLevel, boolean showNameInToolbar )
	{
		if(showNameInToolbar){
			mTopSearchField.setToolbarText( location.getName(), true );
		}

		if( mMapControl != null )
		{
			mMapControl.selectLocation( location, changeZoomLevel );
			currentSelection = location;
			lastSelectionType = LAST_SELECTION_TYPE_SINGLE_LOCATION;
		}
	}

	public void selectLocationsBy( List<Location> locations, int criteriaFlags )
	{
		mSelectedLocationCriteria = null;

		if( (criteriaFlags & QUERY_CATEGORY) != 0 )
		{
			String[] categories = locations.get( 0 ).getCategories();

			if( (categories != null) && (categories.length > 0) )
			{
				mSelectedLocationCriteria = categories[0].toUpperCase( Locale.ROOT );
			}
		}

		if( TextUtils.isEmpty( mSelectedLocationCriteria ) )
		{
			clearSelection();
		}
		else
		{
			mTopSearchField.setToolbarText( mSelectedLocationCriteria, true );

			if( mMapControl != null )
			{
				mMapControl.selectFloor( locations.get( 0 ).getFloorIndex() );
				mMapControl.displaySearchResults(
						locations,
						true,
						MapsIndoorsSettings.DISPLAY_SEARCH_RESULTS_CAMERA_PADDING_IN_DP
				);
			}
		}

		currentSelection = locations;
		lastCriteriaFlags = criteriaFlags;
		lastSelectionType = LAST_SELECTION_TYPE_NEAREST_POSITION;
	}

	public void selectSearchResult( List<Location> locations )
	{
		mMapControl.displaySearchResults(
				locations,
				true,
				MapsIndoorsSettings.DISPLAY_SEARCH_RESULTS_CAMERA_PADDING_IN_DP
		);

		currentSelection = locations;
		lastSelectionType = LAST_SELECTION_TYPE_SEARCH_RESULT;
	}

	@SuppressWarnings("unchecked")
	public void selectLastSelection()
	{
		switch( lastSelectionType )
		{
			case LAST_SELECTION_TYPE_SINGLE_LOCATION:
				selectLocation( (Location) currentSelection, true, true );
				break;

			case LAST_SELECTION_TYPE_NEAREST_POSITION:
				selectLocationsBy( (List< Location >) currentSelection, lastCriteriaFlags );
				break;

			case LAST_SELECTION_TYPE_VENUE:
				setCurrentVenue( (Venue) currentSelection );
				break;

			case LAST_SELECTION_TYPE_SEARCH_RESULT:
				selectSearchResult( (List< Location >) currentSelection );
				break;

			default:
				setCurrentVenue( mCurrentVenue );
		}
	}

	// this function is linked to the returnToVenueButton logic, if you need the selection label for other purpose please implement your own method
	public String getSelectionLabelForReturnToVenue()
	{
		switch( lastSelectionType )
		{
			case LAST_SELECTION_TYPE_SINGLE_LOCATION:
				return ((Location) currentSelection).getName();
			default:
				if(mCurrentVenue!= null){
					return mCurrentVenue.getName();
				}
		}
		return "";
	}

	public void selectCurrentVenue()
	{
		setCurrentVenue( mCurrentVenue );
	}

}
