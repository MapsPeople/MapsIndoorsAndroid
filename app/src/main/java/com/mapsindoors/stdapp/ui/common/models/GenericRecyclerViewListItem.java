package com.mapsindoors.stdapp.ui.common.models;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.stdapp.R;

/**
 * Created by Jose J Varó (jjv@mapspeople.com) on 20/Apr/2017.
 */

/**
 * GenericRecyclerViewListItem
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 20/4/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class GenericRecyclerViewListItem
{
	public String mName, mSubText, mDistText;
	public Integer mImgId;
	public Bitmap mImg;
	public Object mObj;

	/** GenericRecyclerViewAdapter.VIEWTYPE_xx */
	public int mViewType;


	public GenericRecyclerViewListItem( String name, Bitmap img, Object obj, int viewType ) {
		mName = name;
		mSubText = mDistText = null;
		mImgId = -1;
		mImg = img;
		mObj = obj;
		mViewType = viewType;
	}

	public GenericRecyclerViewListItem( String name, Integer imgId, Object obj, int viewType ) {
		mName = name;
		mSubText = mDistText = null;
		mImgId = imgId;
		mImg = null;
		mObj = obj;
		mViewType = viewType;
	}

	public GenericRecyclerViewListItem( String name, String subline, double distance, Integer imgId, Object obj, int viewType ) {
		mName = name;
		mSubText = subline;
		mDistText = formatDistanceText( distance );
		mImgId = imgId;
		mImg = null;
		mObj = obj;
		mViewType = viewType;
	}

	public GenericRecyclerViewListItem( String name, String subline, double distance, Bitmap img, Object obj, int viewType ) {
		mName = name;
		mSubText = subline;
		mDistText = formatDistanceText( distance );
		mImgId = -1;
		mImg = img;
		mObj = obj;
		mViewType = viewType;
	}


	@NonNull
	private String formatDistanceText( double distance ) {

		Context ctx = MapsIndoors.getApplicationContext();
		if( ctx == null ) {
			return "";
		}

		if( distance >= 1000 ) {
			return String.format( ctx.getString( R.string.distance_format_kilometers ), (int) Math.round( distance * (1.0 / 1000.0) ) );
		} else {
			return String.format( ctx.getString( R.string.distance_format_meters ), (int) Math.round( distance ) );
		}
	}
}
