package com.mapsindoors.stdapp.ui.common.models;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.Nullable;

import com.mapsindoors.mapssdk.MPBadgeType;
import com.mapsindoors.mapssdk.MPBooking;
import com.mapsindoors.mapssdk.MPBookingService;
import com.mapsindoors.mapssdk.MPBookingsQuery;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnResultAndDataReadyListener;
import com.mapsindoors.mapssdk.OnResultReadyListener;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.stdapp.ui.common.adapters.IconTextListAdapter;

import java.util.Date;
import java.util.List;

/**
 * Created by Jose J Varó (jjv@mapspeople.com) on 20-01-2017.
 */
public class SearchResultItem {
    private String name;
    private String subtext;

    private MPLocation location;

    private IconTextListAdapter.Objtype type;

    /**
     * The MPLocation object if the item is from an indoor location, the Field object from an outdoor one
     */
    private Object obj;

    /**
     * The indoor location icon
     */
    private Bitmap bmp;

    private int imgId;

    private int dist;

    private OnResultReadyListener callback;


    public SearchResultItem(String name, String subtext, IconTextListAdapter.Objtype type, Bitmap bmp, int imgId, Object obj) {
        this.name = name;
        this.subtext = subtext;
        this.type = type;
        this.bmp = bmp;
        this.imgId = imgId;
        this.obj = obj;
    }

    public SearchResultItem(String name, String subtext, IconTextListAdapter.Objtype type, Bitmap bmp, int imgId, Object obj, int dist) {
        this.name = name;
        this.subtext = subtext;
        this.type = type;
        this.bmp = bmp;
        this.imgId = imgId;
        this.obj = obj;
        this.dist = dist;
    }

    public SearchResultItem(String name, MPLocation location, OnResultReadyListener callback, String subtext, IconTextListAdapter.Objtype type, Bitmap bmp, int imgId, Object obj, int dist) {
        this.name = name;
        this.location = location;
        this.subtext = subtext;
        this.type = type;
        this.bmp = bmp;
        this.imgId = imgId;
        this.obj = obj;
        this.dist = dist;
        this.callback = callback;
    }

    public String getName() {
        return name;
    }

    public String getSubtext() {
        return subtext;
    }

    public IconTextListAdapter.Objtype getType() {
        return type;
    }

    @Nullable
    public Bitmap getBmp() {
        return bmp;
    }

    public int getImgId() {
        return imgId;
    }

    public Object getObj() {
        return obj;
    }

    public int getDist() {
        return dist;
    }

    public MPLocation getLocation(){
        return location;
    }
}
