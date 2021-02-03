package com.mapsindoors.stdapp.apis.googleplaces.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * AutoComplete
 * MISDKAND
 * <p>
 * Created by Jose J Varó on 8/4/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class AutoComplete {
    @SerializedName("predictions")
    public List<AutoCompletePredictions> predictions;
    @SerializedName("status")
    public String status;
}
