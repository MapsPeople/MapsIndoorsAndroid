package com.mapsindoors.stdapp.apis.googleplaces.models;

import com.google.gson.annotations.SerializedName;

/**
 * PredictionTerm
 * MISDKAND
 * <p>
 * Created by Jose J Varó on 9/21/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class PredictionTerm
{
	@SerializedName("offset")
	public int offset;
	@SerializedName("value")
	public String value;
}
