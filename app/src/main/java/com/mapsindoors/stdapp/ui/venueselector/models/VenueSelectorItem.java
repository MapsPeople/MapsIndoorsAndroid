package com.mapsindoors.stdapp.ui.venueselector.models;

import android.graphics.Bitmap;

/**
 * VenueSelectorItem
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 13/04/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class VenueSelectorItem {
    private String mId;
    private String mRenderName;
    private Bitmap mImageBmp;

    public VenueSelectorItem(String id, String renderName, Bitmap imageBmp) {
        mId = id;
        mRenderName = renderName;
        mImageBmp = imageBmp;
    }

    public String getId() {
        return mId;
    }

    public String getRenderName() {
        return mRenderName;
    }

    public Bitmap getImageBmp() {
        return mImageBmp;
    }
}
