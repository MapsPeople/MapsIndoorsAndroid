package com.mapsindoors.stdapp.models;

import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;

public class SchemeModel {

    public static final String SCHEME_PROPERTY_DIRECTIONS_ORIGIN_LOCATION = "originLocation";
    public static final String SCHEME_PROPERTY_DIRECTIONS_ORIGIN_POSITION = "origin";
    public static final String SCHEME_PROPERTY_DIRECTIONS_DESTINATION_LOCATION = "destinationLocation";
    public static final String SCHEME_PROPERTY_DIRECTIONS_DESTINATION_POSITION = "destination";
    public static final String SCHEME_PROPERTY_DIRECTIONS_TRAVEL_MODEL = "travelMode";
    public static final String SCHEME_PROPERTY_DIRECTIONS_AVOID = "avoid";

    public static final String SCHEME_PROPERTY_DETAILS_LOCATION_ID = "location";

    private String scheme;
    private String solutionId;
    private String type;
    private HashMap<String, String> parameters;

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(String solutionId) {
        this.solutionId = solutionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    @Nullable
    public String getParameter(String key) {
        if (!parameters.containsKey(key))
            return null;

        return parameters.get(key);
    }

    public boolean validate() {
        if (TextUtils.isEmpty(getType())) {
            return false;
        }

        switch (getType()) {
            case "directions":
                // Check destination exists
                return !TextUtils.isEmpty(getParameter(SCHEME_PROPERTY_DIRECTIONS_DESTINATION_LOCATION)) || !TextUtils.isEmpty(getParameter(SCHEME_PROPERTY_DIRECTIONS_DESTINATION_POSITION));
            case "details":
                return !TextUtils.isEmpty(getParameter(SCHEME_PROPERTY_DETAILS_LOCATION_ID));
            default:
                return false;
        }
    }
}
