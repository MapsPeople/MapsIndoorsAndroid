package com.mapsindoors.stdapp.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mapsindoors.mapssdk.ImageProvider;
import com.mapsindoors.mapssdk.MIImageBatchItem;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.ReadyListener;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.AppConfig;
import com.mapsindoors.mapssdk.MenuInfo;
import com.mapsindoors.mapssdk.POIType;
import com.mapsindoors.mapssdk.Solution;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MPAppConfigManager
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 9/5/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class AppConfigManager
{
	private static final String TAG = AppConfigManager.class.getSimpleName();


	MapControl mMapControl;
	@Nullable ArrayList<MenuInfo> mMainMenuEntries, mFabMenuEntries;
	@Nullable HashMap<String, String> mTranslations, mVenueImages;

	private String mCountryCode, mTitle, mDefaultVenueId, mFeedbackUrl;
	private boolean mHideFabMenu, mHideTravelModeSelector, mPositionDisabled;

	Thread mUIAssetsThread;

	/**
	 * URL to bitmap image cache
	 *
	 * K: imageURL
	 * V: A reference to the resulting bitmap image
	 */
	HashMap<String, MIImageBatchItem> mBitmapImageCache;

	// K: Menu key
	// V: menuinfo obj
	HashMap<String, MenuInfo> mMainMenuHash;
	HashMap<String, MenuInfo> mFabMenuHash;

	// K: POI type name
	// V:
	HashMap<String, POIType> mPOITypesHash;




	public AppConfigManager( @NonNull MapControl mapControl ) {

		mMapControl = mapControl;

		final AppConfig appConfig = MapsIndoors.getAppConfig();
		if( appConfig == null ) {
			return;
		}
		// Usual entries in the appConfig object

		// Menus
		{
			ArrayList<MenuInfo> mainMenuEntries = appConfig.getMenuInfo( "mainmenu" );
			mMainMenuEntries = MapsIndoorsUtils.isNullOrEmpty( mainMenuEntries ) ? null : mainMenuEntries;

			ArrayList<MenuInfo> fabMenuEntries = appConfig.getMenuInfo( "fabmenu" );
			mFabMenuEntries = MapsIndoorsUtils.isNullOrEmpty( fabMenuEntries ) ? null : fabMenuEntries;
		}

		// Venue images
		{
			HashMap<String, String> venueImages = appConfig.getVenueImages();
			HashMap<String, String> tmpVenueImages = MapsIndoorsUtils.isNullOrEmpty( venueImages ) ? null : venueImages;

			// Convert all keys to upper case
			mVenueImages = new HashMap<>( tmpVenueImages.size() );
			for( String s : tmpVenueImages.keySet() ) {
				mVenueImages.put(s.toUpperCase( Locale.ROOT ), tmpVenueImages.get( s ));
			}
		}

		// Translations
		{
			HashMap<String, String> translations = appConfig.getTranslations();
			mTranslations = MapsIndoorsUtils.isNullOrEmpty( translations ) ? null : translations;
		}

		// Per-project custom data
		HashMap<String, String> appSettings = appConfig.getAppSettings();

		mCountryCode            = appSettings.get( "countryCode" );
		mHideFabMenu            = asBoolean( appSettings.get( "hideFabMenu" ) );
		mTitle                  = appSettings.get( "title" );
		mDefaultVenueId         = appSettings.get( "defaultVenue" );
		mHideTravelModeSelector = asBoolean( appSettings.get( "hideTravelModeSelector" ) );
		mFeedbackUrl            = appSettings.get( "feedbackUrl" );
		mPositionDisabled       = asBoolean( appSettings.get( "positioningDisabled" ) );

		//
		mUIAssetsThread = null;

	}

	/**
	 *
	 * @return A non-null value if the list has one or more entries
	 */
	@Nullable
	public ArrayList<MenuInfo> getMainMenuEntries() {
		return mMainMenuEntries;
	}

	/**
	 *
	 * @return A non-null value if the list has one or more entries
	 */
	@Nullable
	public ArrayList<MenuInfo> getFabMenuEntries() {
		return mFabMenuEntries;
	}

	@Nullable
	public HashMap<String, String> getVenuImages() {
		return mVenueImages;
	}

	/**
	 *
	 * @return A non-null value if the map has one or more entries
	 */
	@Nullable
	public HashMap<String, String> getTranslations() {
		return mTranslations;
	}

	/**
	 *
	 * @return
	 */
	@Nullable
	public String getCountryCode() {
		return mCountryCode;
	}

	/**
	 *
	 * @return
	 */
	@Nullable
	public List<String> getCountryCodes() {
		if( mCountryCode != null ) {
			String[] ccArray = mCountryCode.trim().split( "," );
			return Arrays.asList(ccArray);
		} else {
			return null;
		}
	}

	public boolean isFabMenuHidden() {
		return mHideFabMenu;
	}

	public String getTitle() {
		return mTitle;
	}


	public String getDefaultVenueId() {
		return mDefaultVenueId;
	}

	public boolean isTravelModeSelectorHidden() {
		return mHideTravelModeSelector;
	}

	public String getFeedbackUrl() {
		return mFeedbackUrl;
	}

	public boolean isPositionDisabled() {
		return mPositionDisabled;
	}


	//region MAIN MENU
	@Nullable
	public MenuInfo getMainMenuItem( @NonNull String categoryKey ) {
		if( mMainMenuHash != null ) {
			return mMainMenuHash.get( categoryKey.toUpperCase( Locale.ROOT ) );
		}
		return null;
	}

	@Nullable
	public Bitmap getMainMenuIcon( @NonNull String categoryKey )
	{
		if( mMainMenuHash != null ) {
			MenuInfo mi = mMainMenuHash.get( categoryKey.toUpperCase( Locale.ROOT ) );
			if( mi != null ) {
				String imgUrl = mi.getIconUrl();
				if( imgUrl != null ) {
					final MIImageBatchItem ibi = mBitmapImageCache.get( imgUrl );
					return (ibi != null) ? ibi.getBitmap() : null;
				}
			}
		}
		return null;
	}

	@Nullable
	public Bitmap getMainMenuImage( @NonNull String categoryKey )
	{
		if( mMainMenuHash != null ) {
			MenuInfo mi = mMainMenuHash.get( categoryKey.toUpperCase( Locale.ROOT ) );
			if( mi != null ) {
				String imgUrl = mi.getMenuImageUrl();
				if( imgUrl != null ) {
					final MIImageBatchItem ibi = mBitmapImageCache.get( imgUrl );
					return (ibi != null) ? ibi.getBitmap() : null;
				}
			}
		}

		return null;
	}
	//endregion


	//region FAB MENU
	@Nullable
	public MenuInfo getFabMenuItem( @NonNull String categoryKey ) {
		if( mFabMenuHash != null ) {
			return mFabMenuHash.get( categoryKey.toUpperCase( Locale.ROOT ) );
		}
		return null;
	}

	@Nullable
	public Bitmap getFabMenuIcon( @NonNull String categoryKey )
	{
		if( mFabMenuHash != null ) {
			MenuInfo mi = mFabMenuHash.get( categoryKey.toUpperCase( Locale.ROOT ) );
			if( mi != null ) {
				String imgUrl = mi.getIconUrl();
				if( imgUrl != null ) {
					final MIImageBatchItem ibi = mBitmapImageCache.get( imgUrl );
					return (ibi != null) ? ibi.getBitmap() : null;
				}
			}
		}
		return null;
	}

	@Nullable
	public Bitmap getFabMenuImage( @NonNull String categoryKey )
	{
		if( mFabMenuHash != null ) {
			MenuInfo mi = mFabMenuHash.get( categoryKey.toUpperCase( Locale.ROOT ) );
			if( mi != null ) {
				String imgUrl = mi.getMenuImageUrl();
				if( imgUrl != null ) {
					final MIImageBatchItem ibi = mBitmapImageCache.get( imgUrl );
					return (ibi != null) ? ibi.getBitmap() : null;
				}
			}
		}
		return null;
	}
	//endregion


	//region VENUE IMAGES
	/**
	 *
	 * @param venueCodeName The venue's codename - Venue.name (key/codename), not Venue.venueInfo.name (display name)
	 * @return
	 */
	@Nullable
	public Bitmap getVenueImage( @NonNull String venueCodeName ) {
		if( mVenueImages != null ) {
			String imgUrl = mVenueImages.get( venueCodeName.toUpperCase( Locale.ROOT ) );
			if( imgUrl != null ) {
				final MIImageBatchItem ibi = mBitmapImageCache.get( imgUrl );
				return (ibi != null) ? ibi.getBitmap() : null;
			}
		}
		return null;
	}
	//endregion

	@Nullable
	public Bitmap getPOITypeIcon( @Nullable String name )
	{
		if( (mPOITypesHash != null) && (!TextUtils.isEmpty( name )) )
		{
			POIType poiType = mPOITypesHash.get( name.toUpperCase( Locale.ROOT ) );
			if( poiType != null )
			{
				if( poiType.getIcon() != null )
				{
					final MIImageBatchItem ibi = mBitmapImageCache.get( poiType.getIcon() );
					return (ibi != null) ? ibi.getBitmap() : null;
				}
			}
		}
		return null;
	}

	//endregion


	/**
	 * String to boolean
	 * @param str
	 * @return True if the string is "true" ( str is set to lower case first), 1, or yes. False on any other value
	 */
	private boolean asBoolean( @Nullable String str ) {
		if( TextUtils.isEmpty( str ) ) {
			return false;
		}
		// str is true or false
		String src = str.toLowerCase( Locale.ROOT );
		return src.equals( "true" ) || src.equals( "1" ) || src.equals( "yes" );
	}

	/**
	 * Fetches menu info icons, venue images and POI Type icons
	 *
	 * @param context
	 * @param readyListener
	 * @return
	 */
	@NonNull
	public Thread getUIAssets( final Context context, @NonNull final ReadyListener readyListener )
	{
		mUIAssetsThread = new Thread( () -> {

			mBitmapImageCache = new HashMap<>();

			List<String> imageUrlList = new ArrayList<>();

			if( mMainMenuEntries != null )
			{
				mMainMenuHash = new HashMap<>( mMainMenuEntries.size() );

				for( MenuInfo item : mMainMenuEntries ) {
					final String catKey = item.getCategoryKey();
//						final String name = item.getName();

					mMainMenuHash.put( catKey.toUpperCase( Locale.ROOT ), item );

					final String iconUrl = item.getIconUrl();
					if( !TextUtils.isEmpty( iconUrl ) ) {
						imageUrlList.add( iconUrl );
					}
				}
			}

			if( mFabMenuEntries != null )
			{
				mFabMenuHash = new HashMap<>( mFabMenuEntries.size() );

				for( MenuInfo item : mFabMenuEntries ) {
					final String catKey = item.getCategoryKey();
					mFabMenuHash.put( catKey.toUpperCase( Locale.ROOT ), item );

					final String iconUrl = item.getIconUrl();
					if( !TextUtils.isEmpty( iconUrl ) ) {
						imageUrlList.add( iconUrl );
					}

				}
			}

			// ## AppConfig venue images
			if( mVenueImages != null )
			{
				List< String > list = new ArrayList<>( mVenueImages.keySet() );
				for( String imgKey : list ) {

					final String imageUrl = mVenueImages.get( imgKey );
					if( !TextUtils.isEmpty( imageUrl ) )
					{
						imageUrlList.add( imageUrl );
					}
				}
			}

			// ## POI Type icons
			Solution solution = MapsIndoors.getSolution();

			if( solution != null )
			{
				List<POIType> poiTypes = solution.getTypes();

				if( poiTypes != null ) {
					int poiTypesCount = poiTypes.size();
					mPOITypesHash = new HashMap<>( poiTypesCount );

					for( POIType poiType : poiTypes ) {

						final String iconURL = poiType.getIcon();
						if( !TextUtils.isEmpty( iconURL ) )
						{
							imageUrlList.add( iconURL );
						}

						final String name = poiType.getName();
						mPOITypesHash.put( name.toUpperCase( Locale.ROOT ), poiType );
					}
				}
			}

			//
			if( mUIAssetsThread.isInterrupted() ) {
				return;
			}

			//
			ImageProvider imageProvider = MapsIndoors.getImageProvider();

			imageProvider.loadImagesAsync( imageUrlList, ( result, error ) -> {

				if( result != null )
				{
					for( Map.Entry<String, MIImageBatchItem> resItem : result.entrySet() )
					{
						mBitmapImageCache.put(resItem.getKey(), resItem.getValue() );
					}
				}

				if( BuildConfig.DEBUG )
				{
					// Validate the results...
					for( String imageURL : mBitmapImageCache.keySet() )
					{
						final MIImageBatchItem ibi = mBitmapImageCache.get( imageURL );

						int httpStatus = ibi.getHttpStatus();

						switch( httpStatus )
						{
							case HttpURLConnection.HTTP_OK:
							case HttpURLConnection.HTTP_NOT_MODIFIED:
								break;
							default:
							{
								if(ibi.getBitmap() == null)
								{
									dbglog.LogE( TAG, "getUIAssets() -> ERROR, imageURL: " + imageURL );
								}
							}
						}
					}
				}

				readyListener.onResult();
			} );
		});

		return mUIAssetsThread;
	}
}
