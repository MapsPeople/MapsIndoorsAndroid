package com.mapsindoors.stdapp.apis.googleplaces.models;


import com.google.gson.annotations.SerializedName;

/**
 * GeoCodeResult
 * MISDKAND
 * <p>
 * Created by Jose J Varó on 8/4/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class GeoCodeResult
{
	@SerializedName("formatted_address")
	public String formatted_address;
	@SerializedName("geometry")
	public GeoCodeGeometry geometry;
	@SerializedName("place_id")
	public String place_id;
}
