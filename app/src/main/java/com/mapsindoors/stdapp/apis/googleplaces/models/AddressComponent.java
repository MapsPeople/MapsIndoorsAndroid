package com.mapsindoors.stdapp.apis.googleplaces.models;

import com.google.gson.annotations.SerializedName;

/**
 * AddressComponent
 * MISDKAND
 * <p>
 * Created by Amine on 15/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class AddressComponent
{
	@SerializedName("long_name")
	public String long_name;
	@SerializedName("short_name")
	public String short_name;
	@SerializedName("types")
	public String[] types;
}
