package com.mapsindoors.stdapp.ui.common.listeners;

import com.mapsindoors.mapssdk.Route;

public interface RouteTrackingAdapter {

    void routeSegmentPathChanged(Route route, int legIndex, int stepIndex);

    void selectRouteLeg(Route route, int legIndex);
}
