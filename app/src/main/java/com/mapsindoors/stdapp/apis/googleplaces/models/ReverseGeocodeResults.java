package com.mapsindoors.stdapp.apis.googleplaces.models;

import com.google.gson.annotations.SerializedName;

/**
 * ReverseGeocodeResults
 * MISDKAND
 * <p>
 * Created by Amine on 15/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class ReverseGeocodeResults
{
	@SerializedName("results")
	public ReverseGeocodeResult[] results;
}
