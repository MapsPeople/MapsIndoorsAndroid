package com.mapsindoors.stdapp.apis.googleplaces.models;

import com.google.gson.annotations.SerializedName;

/**
 * ReverseGeocodeResult
 * MISDKAND
 * <p>
 * Created by Amine on 15/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class ReverseGeocodeResult {
    @SerializedName("address_components")
    public AddressComponent[] address_components;
    @SerializedName("formatted_address")
    public String formattedAddress;
}
