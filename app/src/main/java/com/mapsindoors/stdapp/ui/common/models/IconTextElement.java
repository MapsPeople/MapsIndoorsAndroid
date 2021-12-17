package com.mapsindoors.stdapp.ui.common.models;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.DrawableRes;

import com.mapsindoors.mapssdk.BadgePosition;
import com.mapsindoors.mapssdk.MPBooking;
import com.mapsindoors.mapssdk.MPBookingService;
import com.mapsindoors.mapssdk.MPBookingsQuery;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnResultAndDataReadyListener;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.stdapp.helpers.MapsIndoorsRouteHelper;
import com.mapsindoors.stdapp.ui.common.adapters.IconTextListAdapter;

import java.util.Date;
import java.util.List;

public class IconTextElement {
    private String mName;
    private String mSubText;
    private String mDistText;

    @DrawableRes
    private Integer mImgId;

    private Bitmap mImg;
    private Object mObj;
    private IconTextListAdapter.Objtype mType;

    private MPLocation mLocation;
    private Runnable mRequestUiUpdate;

    public IconTextElement(String name, Integer imgId, Object obj, IconTextListAdapter.Objtype type) {
        mName = name;
        mImgId = imgId;
        mImg = null;
        mObj = obj;
        mType = type;
    }

    public IconTextElement(String name, String subline, @DrawableRes Integer imgId, Object obj, IconTextListAdapter.Objtype type) {
        mName = name;
        mSubText = subline;
        mImgId = imgId;
        mImg = null;
        mObj = obj;
        mType = type;
    }

    public IconTextElement(String name, Bitmap img, Object obj, IconTextListAdapter.Objtype type) {
        mName = name;
        mImgId = -1;
        mImg = img;
        mObj = obj;
        mType = type;
    }

    public IconTextElement(String name, String subline, double distance, @DrawableRes Integer imgId, Object obj, IconTextListAdapter.Objtype type) {
        mName = name;
        mSubText = subline;
        mDistText = MapsIndoorsRouteHelper.getFormattedDistance((int) distance);
        mImgId = imgId;
        mImg = null;
        mObj = obj;
        mType = type;
    }

    public IconTextElement(String name, String subline, double distance, Bitmap img, Object obj, IconTextListAdapter.Objtype type) {
        mName = name;
        mSubText = subline;
        mDistText = MapsIndoorsRouteHelper.getFormattedDistance((int) distance);
        mImgId = -1;
        mImg = img;
        mObj = obj;
        mType = type;
    }

    public IconTextElement(String name, String subline, int distance, @DrawableRes Integer imgId, Object obj, IconTextListAdapter.Objtype type) {
        mName = name;
        mSubText = subline;
        mDistText = MapsIndoorsRouteHelper.getFormattedDistance(distance);
        mImgId = imgId;
        mImg = null;
        mObj = obj;
        mType = type;
    }

    public IconTextElement(String name, String subline, int distance, Bitmap img, Object obj, IconTextListAdapter.Objtype type) {
        mName = name;
        mSubText = subline;
        mDistText = MapsIndoorsRouteHelper.getFormattedDistance(distance);
        mImgId = -1;
        mImg = img;
        mObj = obj;
        mType = type;
    }

    public IconTextElement(String name, String subline, int distance, Bitmap img, Object obj, IconTextListAdapter.Objtype type, MPLocation location, Runnable requestUiUpdate) {
        mName = name;
        mSubText = subline;
        mDistText = MapsIndoorsRouteHelper.getFormattedDistance(distance);
        mImgId = -1;
        mImg = img;
        mObj = obj;
        mType = type;
        mLocation = location;
        mRequestUiUpdate = requestUiUpdate;

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

    private void onBookingDetermined(boolean booked){
        if(booked){
            mImg = MapsIndoors.getBadgedAvailabilityIcon(mLocation, mImg, BadgePosition.bottomLeft, false, Color.GREEN, Color.RED, 0.4f);
        } else {
            mImg = MapsIndoors.getBadgedAvailabilityIcon(mLocation, mImg, BadgePosition.bottomLeft, true, Color.GREEN, Color.RED, 0.4f);
        }
        mRequestUiUpdate.run();
    }


    public String getName(){
        return mName;
    }

    public String getSubText(){
        return mSubText;
    }

    public String getDistText(){
        return mDistText;
    }

    public int getImageId(){
        return mImgId;
    }

    public Bitmap getImg(){
        return mImg;
    }

    public IconTextListAdapter.Objtype getObjType(){
        return mType;
    }

    public Object getObj(){
        return mObj;
    }

}
