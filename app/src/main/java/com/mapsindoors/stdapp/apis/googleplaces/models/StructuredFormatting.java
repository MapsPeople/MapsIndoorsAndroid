package com.mapsindoors.stdapp.apis.googleplaces.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * StructuredFormatting
 * MISDKAND
 * <p>
 * Created by Jose J Varó on 8/4/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class StructuredFormatting {
    @SerializedName("main_text")
    public String main_text;
    @SerializedName("main_text_matched_substrings")
    public List<PredictionSubstring> main_text_matched_substrings;
    @SerializedName("secondary_text")
    public String secondary_text;
}
