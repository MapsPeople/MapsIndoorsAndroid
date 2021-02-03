package com.mapsindoors.stdapp.ui.common.models;

import android.graphics.Bitmap;
import androidx.annotation.Nullable;

import com.mapsindoors.stdapp.ui.common.adapters.IconTextListAdapter;

/**
 * Created by Jose J Varó (jjv@mapspeople.com) on 20-01-2017.
 */
public class SearchResultItem {
    private String name;
    private String subtext;

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
}
