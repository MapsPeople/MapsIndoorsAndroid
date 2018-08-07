package com.mapsindoors.stdapp.helpers;

import java.util.Locale;

public class LocaleSettings {


    public static int DISTANCE_UNIT_IMPERIAL = 0;
    public static int DISTANCE_UNIT_METRIC = 1;


    public static int getDefaultDistanceMeasureUnit() {
        return getFrom(Locale.getDefault());
    }

    public static int getFrom(Locale locale) {

        String countryCode = locale.getCountry();
        if ("US".equals(countryCode)){ return DISTANCE_UNIT_IMPERIAL;} // USA
        if ("LR".equals(countryCode)){ return DISTANCE_UNIT_IMPERIAL;} // liberia
        if ("MM".equals(countryCode)){ return DISTANCE_UNIT_IMPERIAL;} // burma

        return DISTANCE_UNIT_METRIC;
    }
}
