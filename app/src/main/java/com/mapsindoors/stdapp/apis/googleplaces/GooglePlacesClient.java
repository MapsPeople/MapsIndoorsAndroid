package com.mapsindoors.stdapp.apis.googleplaces;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.mapsindoors.mapssdk.JSONUtil;
import com.mapsindoors.mapssdk.MIConnectivityUtils;
import com.mapsindoors.mapssdk.URITemplate;
import com.mapsindoors.mapssdk.UrlLoader;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.stdapp.apis.googleplaces.listeners.AutoCompleteSuggestionListener;
import com.mapsindoors.stdapp.apis.googleplaces.listeners.GeoCodeResultListener;
import com.mapsindoors.stdapp.apis.googleplaces.listeners.ReverseGeoCodeResultListener;
import com.mapsindoors.stdapp.apis.googleplaces.models.AutoComplete;
import com.mapsindoors.stdapp.apis.googleplaces.models.AutoCompleteField;
import com.mapsindoors.stdapp.apis.googleplaces.models.AutoCompletePredictions;
import com.mapsindoors.stdapp.apis.googleplaces.models.GeocodeResults;
import com.mapsindoors.stdapp.apis.googleplaces.models.ReverseGeocodeResults;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GooglePlacesClient
 * MISDKAND
 * <p>
 * Created by Jose J Varó on 6/14/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public class GooglePlacesClient
{
	private static final String TAG = GooglePlacesClient.class.getSimpleName();

	// =================================================================================
	// Guide:  https://developers.google.com/maps/documentation/javascript/places-autocomplete
	// Ref:    https://developers.google.com/maps/documentation/javascript/3.exp/reference
	// =================================================================================

	private static final String GOOGLE_MAPS_APIS_BASE_URL = "https://maps.googleapis.com/maps/api";

	// Ref: https://developers.google.com/places/web-service/autocomplete
	private static final String AUTOCOMPLETE_URL_TEMPLATE_EXTERNAL = GOOGLE_MAPS_APIS_BASE_URL + "/place/autocomplete/json?";

	// Ref: https://developers.google.com/places/web-service/details
	private static final String LOCATION_URL_TEMPLATE_EXTERNAL = GOOGLE_MAPS_APIS_BASE_URL + "/geocode/json?place_id={place_id}";

	// Ref: https://developers.google.com/maps/documentation/javascript/geocoding#ReverseGeocoding
	private static final String REVERSE_GEOCODE_URL_TEMPLATE_EXTERNAL = GOOGLE_MAPS_APIS_BASE_URL + "/geocode/json?latlng={lat},{lng}&key={key}";

	private static final String URL_COMPONENT_KEY = "key";
	private static final String URL_COMPONENT_LAT = "lat";
	private static final String URL_COMPONENT_LNG = "lng";


	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@StringDef({PLACE_TYPE_GEOCODE, PLACE_TYPE_ADDRESS, PLACE_TYPE_ESTABLISHMENT, PLACE_TYPE_REGIONS, PLACE_TYPE_CITIES})
	public @interface PlaceTypes{}
	public static final String PLACE_TYPE_GEOCODE           = "geocode";
	public static final String PLACE_TYPE_ADDRESS           = "address";
	public static final String PLACE_TYPE_ESTABLISHMENT     = "establishment";
	public static final String PLACE_TYPE_REGIONS           = "(regions)";
	public static final String PLACE_TYPE_CITIES            = "(cities)";

	// ...


	private HashMap< String, String > mGenerateUrlParams;
	private StringBuilder mGenerateUrlStringBuilder;
	private JSONUtil mJsonUtil;


	public GooglePlacesClient() {
		mGenerateUrlParams = (mGenerateUrlParams != null) ? mGenerateUrlParams : new HashMap<>( 5 );
		mGenerateUrlStringBuilder = (mGenerateUrlStringBuilder != null) ? mGenerateUrlStringBuilder : new StringBuilder();
		mJsonUtil = new JSONUtil();
	}

	/**
	 * Returns a list of "autocomplete" strings based on the given data (input string, location, radius). It uses the Google Places Autocomplete API to do so.
	 * <br>
	 * For more info, check <a href="https://developers.google.com/places/web-service/autocomplete">Place Autocomplete Requests</a>
	 * <br>
	 * Components, (country codes): <a href="https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2">ISO 3166-1 Alpha-2</a>
	 *
	 * @param params
	 * @param callback
	 */
	public void getAutoCompleteSuggestions( @NonNull AutoCompleteParams params, @NonNull final AutoCompleteSuggestionListener callback )
	{
		// Return an empty list if the app can't go online...
		if( !MIConnectivityUtils.isOnline() ) {
			callback.onResult( new ArrayList<>(), "" );
			return;
		}

		mGenerateUrlStringBuilder.setLength( 0 );
		StringBuilder url = mGenerateUrlStringBuilder.append( AUTOCOMPLETE_URL_TEMPLATE_EXTERNAL );

		// Mandatory fields
		url.append( "input=" ).append( params.input );
		url.append( "&key=" ).append( params.googleKey );

		// Optional fields
		if( params.language != null ) {
			url.append( "&language=" ).append( params.language );
		}

		if( params.location != null ) {
			url.append( "&location=" ).
					append( params.location.getLat() ).
					append( "," ).
					append( params.location.getLng() );
		}

		if( params.radius > 0 ) {
			url.append( "&radius=" ).append( params.radius );
		}

		if( params.offset > 0 ) {
			url.append( "&offset=" ).append( params.offset );
		}

		// Countries are optional
		// ...&components=country:us|country:pr|country:vi|country:gu|country:mp
		if( (params.components != null) && (params.components.size() > 0) ) {
			StringBuilder sb = new StringBuilder();

			sb.append( "country:" ).append( params.components.get( 0 ) );

			for( int i = 1, aLen = params.components.size(); i < aLen; i++ ) {
				sb.append( "|country:" ).append( params.components.get( i ) );
			}

			url.append( "&components=" ).append( sb );
		}

		if( params.strictbounds ) {
			url.append( "&strictbounds" );
		}

		if( params.placeType != null ) {
			url.append( "&types=" ).append( params.placeType );
		}

		UrlLoader loader = new UrlLoader()
		{
			public void onResult( String content, int status ) {
				AutoComplete result = null;
				String resultStatus = "";

				if( (status < HttpURLConnection.HTTP_MULT_CHOICE) && (content != null) ) {

					result = new Gson().fromJson(
							content,
							AutoComplete.class
					);
				}

				List<AutoCompleteField> resultList = new ArrayList<>();

				if( result != null && result.predictions != null ) {

					resultStatus = result.status;

					for( AutoCompletePredictions prediction : result.predictions ) {
						AutoCompleteField field = new AutoCompleteField();
						field.placeId = prediction.place_id;
						field.mainText = prediction.structured_formatting.main_text;
						field.secondaryText = prediction.structured_formatting.secondary_text;
						resultList.add( field );
					}
				}

				callback.onResult( resultList, resultStatus );
			}
		};

		loader.readUrl( url.toString() );
	}

	/**
	 * Returns details (using Google's Places Details API) for the given place Id
	 *
	 * @param placeId A google Place Id (https://developers.google.com/places/web-service/place-id)
	 * @param callback
	 */
	public void getPlaceDetails( @NonNull final String placeId, @NonNull String googleKey, @NonNull final GeoCodeResultListener callback ) {
		// Return a null value if there app can't go online...
		if( !MIConnectivityUtils.isOnline() ) {
			callback.onResult( null );
			return;
		}

		URITemplate t = new URITemplate( LOCATION_URL_TEMPLATE_EXTERNAL );

		mGenerateUrlParams.clear();

		Map< String, String > parametersMap = mGenerateUrlParams;
		parametersMap.put( "place_id", placeId );

		//
		mGenerateUrlStringBuilder.setLength( 0 );
		StringBuilder queryUrl = mGenerateUrlStringBuilder.append( t.generate( parametersMap ) );

		//Using google maps. Sign or use a mGoogleServerKey if possible.
		queryUrl.append( "&key=" ).append( googleKey );

		UrlLoader loader = new UrlLoader() {
			public void onResult( String content, int status ) {
				GeocodeResults callbackResults = null;

				if( (status < HttpURLConnection.HTTP_MULT_CHOICE) && (content != null) ) {

					//callbackResults = mJsonUtil.deserialize( content, GeocodeResults.class );
					callbackResults = new Gson().fromJson(
							content,
							GeocodeResults.class
					);
				}

				callback.onResult( callbackResults );
			}
		};

		loader.readUrl( queryUrl.toString() );
	}

	/**
	 * Does this, this and that
	 *
	 * @param location
	 * @param googleKey
	 * @param callback
	 */
	public void getLatLngAddress( @NonNull final LatLng location, @NonNull String googleKey, @NonNull final ReverseGeoCodeResultListener callback ) {
		// Return a null value if there app can't go online...
		if( !MIConnectivityUtils.isOnline() ) {
			callback.onResult( null );
			return;
		}

		URITemplate t = new URITemplate( REVERSE_GEOCODE_URL_TEMPLATE_EXTERNAL );

		mGenerateUrlParams.clear();

		Map<String, String> parametersMap = mGenerateUrlParams;
		parametersMap.put( URL_COMPONENT_KEY, googleKey );
		parametersMap.put( URL_COMPONENT_LAT, "" + location.latitude );
		parametersMap.put( URL_COMPONENT_LNG, "" + location.longitude );

		//
		mGenerateUrlStringBuilder.setLength( 0 );

		StringBuilder queryUrl = mGenerateUrlStringBuilder.append( t.generate( parametersMap ) );

		UrlLoader loader = new UrlLoader() {
			public void onResult( String content, int status ) {
				ReverseGeocodeResults callbackResult = null;

				if( (status < HttpURLConnection.HTTP_MULT_CHOICE) && (content != null) ) {

					callbackResult = new Gson().fromJson(
							content,
							ReverseGeocodeResults.class
					);
				}

				callback.onResult( callbackResult );
			}
		};

		loader.readUrl( queryUrl.toString() );
	}


	//region PLACES AUTOCOMPLETE BUILDER
	public static final class AutoCompleteParamsBuilder
	{
		AutoCompleteParams params;

		public AutoCompleteParamsBuilder( @NonNull String input, @NonNull String googleKey ) {
			params = new AutoCompleteParams();
			params.input = input;
			params.googleKey = googleKey;
		}

		// in a setter: IllegalArgumentException
		//
		@NonNull
		public AutoCompleteParamsBuilder setOffset( @IntRange(from=0) int offset ) {
			params.offset = offset;
			return this;
		}

		@NonNull
		public AutoCompleteParamsBuilder setLocation( @NonNull Point location ) {
			params.location = location;
			return this;
		}

		@NonNull
		public AutoCompleteParamsBuilder setRadius( @IntRange(from=0) int radius ) {
			params.radius = radius;
			return this;
		}

		@NonNull
		public AutoCompleteParamsBuilder setLanguage( @NonNull String language ) {
			params.language = language;
			return this;
		}

		@NonNull
		public AutoCompleteParamsBuilder setTypes( @NonNull String types ) {
			params.types = types;
			return this;
		}

		@NonNull
		public AutoCompleteParamsBuilder setComponents( @NonNull List<String> components ) {
			params.components = components;
			return this;
		}

		@NonNull
		public AutoCompleteParamsBuilder setStrictbounds( boolean strictbounds ) {
			params.strictbounds = strictbounds;
			return this;
		}

		@NonNull
		public AutoCompleteParamsBuilder setPlaceType( @NonNull @PlaceTypes String placeType ) {
			params.placeType = placeType;
			return this;
		}

		@NonNull
		public AutoCompleteParams build() {
			// IllegalStateException
			return params;
		}
	}

	public static class AutoCompleteParams
	{
		// Required parameters
		String input;
		String googleKey;

		// Optional parameters
		int            offset;
		Point          location;
		int            radius;
		String         language;
		String         types;
		List< String > components;
		boolean        strictbounds;
		@PlaceTypes String placeType;
	}
	//endregion
}
