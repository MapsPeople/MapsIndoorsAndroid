package com.mapsindoors.stdapp.apis.googleplaces.models;

import com.google.gson.annotations.SerializedName;

/**
 * AutoCompleteField
 * MISDKAND
 * <p>
 * Created by Jose J Varó on 8/4/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class AutoCompleteField
{
	@SerializedName("mainText")
	public String mainText;
	@SerializedName("secondaryText")
	public String secondaryText;
	@SerializedName("placeId")
	public String placeId;
}
