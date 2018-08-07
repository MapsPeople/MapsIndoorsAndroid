package com.mapsindoors.stdapp.apis.googleplaces.listeners;

import com.mapsindoors.stdapp.apis.googleplaces.models.ReverseGeocodeResults;

/**
 * ReverseGeoCodeResultListener
 * MISDKAND
 * <p>
 * Created by Amine on 16/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public interface ReverseGeoCodeResultListener
{
	void onResult( ReverseGeocodeResults reverseGeocodeResults );
}
