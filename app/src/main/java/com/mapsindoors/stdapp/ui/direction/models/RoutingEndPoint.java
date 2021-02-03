package com.mapsindoors.stdapp.ui.direction.models;

import android.content.Context;
import androidx.annotation.NonNull;

import com.mapsindoors.mapssdk.Building;
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.LocationPropertyNames;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.Venue;
import com.mapsindoors.mapssdk.VenueCollection;
import com.mapsindoors.mapssdk.VenueInfo;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;

import org.jetbrains.annotations.Nullable;

/**
 * Created by amine on 23/11/2017.
 */

public class RoutingEndPoint {

    public static final int ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING = 0;
    public static final int ENDPOINT_TYPE_MY_POSITION_OUTSIDE_BUILDING = 1;
    public static final int ENDPOINT_TYPE_AUTOCOMPLETE = 2;
    public static final int ENDPOINT_TYPE_POI = 3;


    MPLocation location;
    private String details;
    int type;


    public RoutingEndPoint(@Nullable MPLocation loc, @NonNull String details, int type) {
        this.location = loc;
        this.details = details;
        this.type = type;
    }

    @Nullable
    public MPLocation getLocation() {
        return location;
    }

    @NonNull
    public String getDetails() {
        return details;
    }

    public int getType() {
        return type;
    }

    public void setLocation(@Nullable MPLocation location) {
        this.location = location;
    }

    public void setDetails(@NonNull String details) {
        this.details = details;
    }

    public void setType(int type) {
        this.type = type;
    }

    @NonNull
    public String getLocationName(@NonNull Context context) {
        if (type == ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING || ENDPOINT_TYPE_MY_POSITION_OUTSIDE_BUILDING == type) {
            return context.getResources().getString(R.string.my_position);
        } else if (location != null) {
            return location.getName();
        } else {
            return "";
        }

    }

    @Nullable
    public String getFormattedDetails(@NonNull MapsIndoorsActivity activity) {
        switch (type) {
            case ENDPOINT_TYPE_POI:
            case ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING: {
                // If this is a normal location or my location and it's inside a building compose building & venue names such as the following: ([buildingName, venueName])
                final BuildingCollection bc = activity.getBuildingCollection();
                final VenueCollection vc = activity.getVenueCollection();

                if ((bc == null) || (vc == null)) {
                    return null;
                }

                final String bName;
                final Building b = bc.getBuildingByAdminId((String) location.getProperty(LocationPropertyNames.BUILDING));
                if (b != null) {
                    bName = b.getName();
                } else {
                    bName = null;
                }

                final String vName;
                final Venue v = vc.getVenueByName((String) location.getProperty(LocationPropertyNames.VENUE));
                if (v != null) {
                    VenueInfo vInfo = v.getVenueInfo();
                    vName = vInfo.getName();
                } else {
                    vName = null;
                }

                //
                boolean firstInLigne = true;
                String resDetails = null;

                if (type == ENDPOINT_TYPE_MY_POSITION_INSIDE_BUILDING) {
                    resDetails = location.getName();
                    firstInLigne = false;
                }

                final String floor = location.getFloorName();
                if (floor != null) {
                    if (firstInLigne) {
                        resDetails = String.format(activity.getResources().getString(R.string.routing_endpoint_level_format_start), floor);
                        firstInLigne = false;
                    } else {
                        resDetails = String.format(activity.getResources().getString(R.string.routing_endpoint_level_format_middle), resDetails, floor);
                    }
                }

                //
                if (bName != null) {
                    if (firstInLigne) {
                        resDetails = bName;
                        firstInLigne = false;
                    } else {
                        resDetails = String.format(activity.getResources().getString(R.string.routing_endpoint_field_format_middle), resDetails, bName);
                    }
                }

                //
                if (vName != null) {
                    if (firstInLigne) {
                        resDetails = vName;
                    } else {
                        resDetails = String.format(activity.getResources().getString(R.string.routing_endpoint_field_format_middle), resDetails, vName);
                    }
                }

                return resDetails;
            }
            case ENDPOINT_TYPE_AUTOCOMPLETE:
            case ENDPOINT_TYPE_MY_POSITION_OUTSIDE_BUILDING: {
                return details;
            }
        }

        return null;
    }
}
