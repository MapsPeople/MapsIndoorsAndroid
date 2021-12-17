package com.mapsindoors.stdapp.ui.common.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.mapsindoors.mapssdk.BadgePosition;
import com.mapsindoors.mapssdk.MPBooking;
import com.mapsindoors.mapssdk.MPBookingService;
import com.mapsindoors.mapssdk.MPBookingsQuery;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnResultAndDataReadyListener;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.stdapp.R;

import java.util.Date;
import java.util.List;

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

	private Runnable mRequestUiUpdate;

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

	public GenericRecyclerViewListItem(String name, String subline, double distance, Bitmap img, Object obj, int viewType, Runnable requestUiUpdate) {
		mName = name;
		mSubText = subline;
		mDistText = formatDistanceText( distance );
		mImgId = -1;
		mImg = img;
		mObj = obj;
		mViewType = viewType;
		mRequestUiUpdate = requestUiUpdate;

		// Determine if the location is currently booked, and adjust the icon accordingly (with a badged icon)
		if(mObj instanceof MPLocation){
			MPLocation location = (MPLocation) mObj;
			if(((MPLocation) mObj).getBookable()){
				if(location.getBookable()){
					MPBookingsQuery query = new MPBookingsQuery.Builder()
							.setStartTime(new Date(System.currentTimeMillis()))
							.setEndTime(new Date(System.currentTimeMillis() + 1000))
							.setLocation(location)
							.build();
					MPBookingService.getInstance().getBookingsUsingQuery(query, new OnResultAndDataReadyListener<List<MPBooking>>() {
						@Override
						public void onResultReady(List<MPBooking> data, MIError error) {
							if(data == null || data.size() == 0){
								onBookingDetermined(false);
							} else {
								onBookingDetermined(true);
							}
						}
					});
				}
			}
		}
	}

	private void onBookingDetermined(boolean booked){
		if(booked){
			mImg = MapsIndoors.getBadgedAvailabilityIcon((MPLocation) mObj, mImg, BadgePosition.bottomLeft, false, Color.GREEN, Color.RED, 0.4f);
		} else {
			mImg = MapsIndoors.getBadgedAvailabilityIcon((MPLocation) mObj, mImg, BadgePosition.bottomLeft, true, Color.GREEN, Color.RED, 0.4f);
		}
		// Update view
		mRequestUiUpdate.run();
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
