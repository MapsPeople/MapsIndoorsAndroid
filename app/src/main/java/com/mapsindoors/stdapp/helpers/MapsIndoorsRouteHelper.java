package com.mapsindoors.stdapp.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.TransitDetails;
import com.mapsindoors.mapssdk.VenueCollection;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.apis.googleplaces.models.AutoCompleteField;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.ui.common.adapters.IconTextListAdapter;
import com.mapsindoors.stdapp.ui.common.models.SearchResultItem;

import java.util.ArrayList;
import java.util.List;

public class MapsIndoorsRouteHelper
{
	/**
	 * @param duration In seconds
	 * @return
	 */
	@NonNull
	public static String getFormattedDuration( int duration )
	{
		// <=60 seconds: Show duration as 1 min. (Ex 1 min)
		// 60 seconds < duration < 1 hour: Convert duration to integer minutes. (Ex: 1 min, 5 min, 54 min)
		// 1 hour <= duration < 1 day: Convert duration to hours and minutes. (Ex: 1 h 1 min, 3 h 43 min, 17 h 43 min - if zero minutes don't write minutes)
		// >= 1 day: Convert duration to days and hours (rounded up). (Ex: 1 d 4 h, 5 d 20 h, 2 d 4 h - if zero hours don't write hours)

		final Context ctx = MapsIndoors.getApplicationContext();
		if( ctx == null )
		{
			return "";
		}

		Resources res = ctx.getResources();

		int minsVal = duration / 60;
		if( minsVal < 1 )
		{
			return String.format( res.getString( R.string.elapsed_time_format_min ), 1 );
		}

		int secondVal = duration - (minsVal * 60);
		if( secondVal >= 30 )
		{
			minsVal += 1;
		}

		int days = minsVal / (24 * 60);
		minsVal = minsVal - ((24 * 60) * days);

		int hours = minsVal / 60;
		minsVal = minsVal - hours * 60;

		//
		String formattedDuration = "";

		if( hours > 0 )
		{
			if( minsVal > 0 )
			{
				if( days == 0 )
				{
					formattedDuration = String.format( res.getString( R.string.elapsed_time_format_hour_min ), hours, minsVal );
				}
				else
				{
					formattedDuration = String.format( res.getString( R.string.elapsed_time_format_day_hour ), days, hours );
				}
			}
			else
			{
				if( days == 0 )
				{
					formattedDuration = String.format( res.getString( R.string.elapsed_time_format_hour ), hours );
				}
				else
				{
					formattedDuration = String.format( res.getString( R.string.elapsed_time_format_day_hour ), days, hours );
				}
			}
		}
		else
		{
			if( minsVal > 0 )
			{
				formattedDuration = String.format( res.getString( R.string.elapsed_time_format_min ), minsVal );
			}
			else if( days > 0 )
			{
				formattedDuration = String.format( res.getString( R.string.elapsed_time_format_day ), days );
			}
		}

		return formattedDuration.trim();
	}

	/**
	 * Returns a formatted string representing the given distance in either Metric Units (SI) or Imperial units.
	 * The current device locale is used to decide whether the output will be in Metric or Imperial units
	 *
	 * @param distance In meters
	 * @return A formatted string using any of rhe "distance_format_x" string templates
	 */
	@NonNull
	public static String getFormattedDistance( int distance )
	{
		final Context ctx = MapsIndoors.getApplicationContext();
		if( ctx == null )
		{
			return "";
		}

		final boolean usingMetricSystem = LocaleSettings.getDefaultDistanceMeasureUnit() == LocaleSettings.DISTANCE_UNIT_METRIC;

		if( usingMetricSystem )
		{
			if( distance < 100 )
			{
				return String.format( ctx.getString( R.string.distance_format_meters ), distance );
			}
			else
			{
				boolean asFloat;
				float   distInKm = distance * (1.0f / 1000.0f);

				//
				if( distance < (100 * 1000) )
				{
					float df = distInKm - ((int) distInKm);
					asFloat = df >= 0.1f;
				}
				else
				{
					asFloat = false;
				}

				if( asFloat )
				{
					return String.format( ctx.getString( R.string.distance_format_kilometers_f ), distInKm );
				}
				else
				{
					return String.format( ctx.getString( R.string.distance_format_kilometers ), (int) (distInKm) );
				}
			}
		}
		else
		{
			float feetDistance = meterToFoot( distance );

			if( feetDistance < 1000 )
			{
				return String.format( ctx.getString( R.string.distance_format_feets ), (int) feetDistance );
			}
			else
			{
				float milesDistance = feetDistance * (1.0f / 5280.0f);
				float df            = milesDistance - (int) milesDistance;
				if( df >= 0.1 )
				{
					return String.format( ctx.getString( R.string.distance_format_miles_f ), milesDistance );
				}
				else
				{
					return String.format( ctx.getString( R.string.distance_format_miles ), (int) milesDistance );
				}
			}
		}
	}

	private static float meterToFoot( float meter )
	{
		return meter * (1.0f / 0.3048f);
	}

	@NonNull
	public static List< SearchResultItem > indoorLocationToSearchResultItemList( @Nullable List< Location > indoorLocations, @Nullable AppConfigManager appConfigManager, @Nullable BuildingCollection buildingCollection, @Nullable VenueCollection venueCollection )
	{
		final Context            context              = MapsIndoors.getApplicationContext();
		List< SearchResultItem > searchResultItemList = new ArrayList<>();

		if( (indoorLocations != null) && (appConfigManager != null) && (venueCollection != null) )
		{
			for( Location location : indoorLocations )
			{
				searchResultItemList.add( new SearchResultItem( location.getName(), MapsIndoorsHelper.composeLocationInfoString( context, location, venueCollection, buildingCollection ), IconTextListAdapter.Objtype.LOCATION, appConfigManager.getPOITypeIcon( location.getType() ), -1, location ) );
			}
		}

		return searchResultItemList;
	}

	public static List< SearchResultItem > googlePlacesAutocompleteFieldToSearchResultItemList( List< AutoCompleteField > autoCompleteFieldList )
	{
		List< SearchResultItem > searchResultItemList = new ArrayList<>();

		for( AutoCompleteField field : autoCompleteFieldList )
		{
			searchResultItemList.add( new SearchResultItem( field.mainText, field.secondaryText, IconTextListAdapter.Objtype.LOCATION, null, R.drawable.ic_place_black_24dp, field ) );
		}

		return searchResultItemList;
	}

	/**
	 * @param context
	 * @param transitDetails
	 * @return
	 */
	@ColorInt
	public static int getTransitDetailsLineColor( Context context, TransitDetails transitDetails )
	{
		String lineColor = transitDetails.getLine().getColor();

		if( (lineColor == null) || (lineColor.equalsIgnoreCase( "#ffffff" )) )
		{
			return ContextCompat.getColor( context, R.color.transitdetails_line_color_default );
		}
		else
		{
			return Color.parseColor( lineColor );
		}
	}

	/**
	 * @param ctx
	 * @param transitDetails
	 * @return
	 */
	@ColorInt
	public static int getTransitDetailsLineTextColor( Context ctx, TransitDetails transitDetails )
	{
		String lineTextColor = transitDetails.getLine().getText_color();

		if( lineTextColor != null )
		{
			return Color.parseColor( lineTextColor );
		}
		else
		{
			return ContextCompat.getColor( ctx, R.color.transitdetails_line_text_color_default );
		}
	}


}

