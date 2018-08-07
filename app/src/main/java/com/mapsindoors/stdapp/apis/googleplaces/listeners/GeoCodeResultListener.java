package com.mapsindoors.stdapp.apis.googleplaces.listeners;


import com.mapsindoors.stdapp.apis.googleplaces.models.GeocodeResults;

public abstract class GeoCodeResultListener
{
	public abstract void onResult( GeocodeResults results );
}
