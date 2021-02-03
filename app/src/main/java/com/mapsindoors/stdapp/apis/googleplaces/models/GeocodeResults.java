package com.mapsindoors.stdapp.apis.googleplaces.models;

import com.google.gson.annotations.SerializedName;

/**
 * GeoCodeResult
 * MISDKAND
 * <p>
 * Created by Jose J Varó on 8/4/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class GeocodeResults {
    @SerializedName("results")
    public GeoCodeResult[] results;
    @SerializedName("status")
    public String status;
}
