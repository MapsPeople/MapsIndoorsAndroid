package com.mapsindoors.stdapp.apis.googleplaces.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * AutoCompletePredictions
 * MISDKAND
 * <p>
 * Created by Jose J Varó on 8/4/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class AutoCompletePredictions {
    @SerializedName("description")
    public String description;
    @SerializedName("id")
    public String id;
    @SerializedName("matched_substrings")
    public List<PredictionSubstring> matched_substrings;
    @SerializedName("place_id")
    public String place_id;
    @SerializedName("reference")
    public String reference;
    @SerializedName("structured_formatting")
    public StructuredFormatting structured_formatting;
    @SerializedName("terms")
    public List<PredictionTerm> terms;
    @SerializedName("types")
    public List<String> types;
}
