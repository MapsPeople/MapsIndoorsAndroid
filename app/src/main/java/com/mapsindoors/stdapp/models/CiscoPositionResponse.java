package com.mapsindoors.stdapp.models;

import com.google.gson.annotations.SerializedName;
import com.mapsindoors.mapssdk.MPPositionResult;

/**
 * CiscoPositionResponse
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 11/2/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class CiscoPositionResponse
{
	@SerializedName("status")
	private int status;
	@SerializedName("result")
	private MPPositionResult result;


	public MPPositionResult getResult() {
		return result;
	}

	public int getStatus() {
		return status;
	}
}
