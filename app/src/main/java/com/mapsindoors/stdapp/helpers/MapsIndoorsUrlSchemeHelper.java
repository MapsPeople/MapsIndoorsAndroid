package com.mapsindoors.stdapp.helpers;

import android.net.Uri;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.stdapp.models.SchemeModel;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MapsIndoorsUrlSchemeHelper {

    @Nullable
    public static SchemeModel urlSchemeParser(Uri schemeUrl) {
        if(schemeUrl == null)
            return null;

        SchemeModel scheme = new SchemeModel();
        scheme.setScheme(schemeUrl.getScheme());
        List<String> parts = schemeUrl.getPathSegments();

        if(scheme.getScheme().equalsIgnoreCase("http") || scheme.getScheme().equalsIgnoreCase("https")) {
            // Example: https://clients.mapsindoors.com/solutionId/directions

            if (parts.size() == 7 && parts.get(2).equals("route")) {
                // Example: https://clients.mapsindoors.com/{solution}/{venueId}/route/from/{locId}/to/{locId}

                schemeUrl = Uri.parse("https://clients.mapsindoors.com/" + parts.get(0) + "/directions?originLocation=" + parts.get(4) + "&destinationLocation=" + parts.get(6) + "&travelMode=walking");
                scheme.setType("directions");
            } else {
                scheme.setType(parts.get(1));
            }
            scheme.setSolutionId(parts.get(0));
        } else {
            // Example: mapsindoorsapp://solutionId/directions

            scheme.setSolutionId(schemeUrl.getHost());
            scheme.setType(parts.get(0));
        }

        Set<String> queryParams = schemeUrl.getQueryParameterNames();
        HashMap<String, String> parameterMap = new HashMap<>();
        for (String parameterKey : queryParams) {
            parameterMap.put(parameterKey, schemeUrl.getQueryParameter(parameterKey));
        }

        scheme.setParameters(parameterMap);

        boolean schemeIsValid = scheme.validate();
        if(!schemeIsValid) {
            return null;
        }

        return scheme;
    }

    @Nullable
    public static MPLocation createLocationForDirectionsDestination(SchemeModel scheme) {
        String destinationLocationId = scheme.getParameter(SchemeModel.SCHEME_PROPERTY_DIRECTIONS_DESTINATION_LOCATION);
        String destinationPosition = scheme.getParameter(SchemeModel.SCHEME_PROPERTY_DIRECTIONS_DESTINATION_POSITION);

        return createMPLocation(destinationLocationId, destinationPosition);
    }

    @Nullable
    public static MPLocation createLocationForDirectionsOrigin(SchemeModel scheme) {
        String originLocationId = scheme.getParameter(SchemeModel.SCHEME_PROPERTY_DIRECTIONS_ORIGIN_LOCATION);
        String originPosition = scheme.getParameter(SchemeModel.SCHEME_PROPERTY_DIRECTIONS_ORIGIN_POSITION);

        return createMPLocation(originLocationId, originPosition);
    }

    @Nullable
    public static MPLocation createLocationForDetailsLocation(SchemeModel scheme) {
        String locationId = scheme.getParameter(SchemeModel.SCHEME_PROPERTY_DETAILS_LOCATION_ID);

        return createMPLocation(locationId, null);
    }

    @Nullable
    private static MPLocation createMPLocation(@Nullable String locationId, @Nullable  String position) {
        MPLocation location = null;
        if(!TextUtils.isEmpty(locationId)) {
            location = MapsIndoors.getLocationById(locationId);
        } else if(position != null) {
            location = createMPLocationFromPosition(position);
        }

        return location;
    }

    @Nullable
    private static MPLocation createMPLocationFromPosition(String position) {
        try {
            String[] components = position.split(",");
            if (components.length < 2) {
                return null;
            }

            double latitude = Double.parseDouble(components[0]);
            double longitude = Double.parseDouble(components[1]);

            MPLocation.Builder builder = new MPLocation.Builder("Coordinate")
                    .setName(String.format(Locale.UK,"%f,%f", latitude, longitude))
                    .setPosition(new LatLng(latitude, longitude));

            if (components.length == 3) {
                int floorIndex = Integer.parseInt(components[2]);
                builder.setFloor(floorIndex);
            }

            return builder.build();
        } catch (Exception ex) {
            return null;
        }
    }
}

