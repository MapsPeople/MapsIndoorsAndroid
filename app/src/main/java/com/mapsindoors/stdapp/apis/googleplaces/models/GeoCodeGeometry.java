package com.mapsindoors.stdapp.apis.googleplaces.models;


import com.google.gson.annotations.SerializedName;

/**
 * GeoCodeGeometry
 * MISDKAND
 * <p>
 * Created by Jose J Varó on 8/4/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class GeoCodeGeometry
{
	@SerializedName("bounds")
	public MyLatLngBounds bounds;
	@SerializedName("location")
	public MyLatLng location;
	@SerializedName("location_type")
	public String location_type;
	@SerializedName("viewport")
	public MyLatLngBounds viewport;


	public class MyLatLng
	{
		@SerializedName("lat")
		public double lat;
		@SerializedName("lng")
		public double lng;
	}

	public class MyLatLngBounds
	{
		@SerializedName("northeast")
		public MyLatLng northeast;
		@SerializedName("southwest")
		public MyLatLng southwest;
	}
}
