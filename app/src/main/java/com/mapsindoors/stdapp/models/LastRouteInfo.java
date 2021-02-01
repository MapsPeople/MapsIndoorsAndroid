package com.mapsindoors.stdapp.models;

import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.Route;

public class LastRouteInfo {
    private Route mLastRoute;
    private MPLocation mLastRouteOrigin;
    private MPLocation mLastRouteDestination;
    private boolean mIsOriginInsideABuilding;
    private boolean mIsDestinationInsideABuilding;
    private long mTimeStamp;

    public LastRouteInfo(Route lastRoute, MPLocation lastRouteOrigin, MPLocation lastRouteDestination, boolean isOriginInsideABuilding, boolean isDestinationInsideABuilding, long timeStamp) {
        mLastRoute = lastRoute;
        mLastRouteOrigin = lastRouteOrigin;
        mLastRouteDestination = lastRouteDestination;
        mIsOriginInsideABuilding = isOriginInsideABuilding;
        mIsDestinationInsideABuilding = isOriginInsideABuilding;
        mTimeStamp = timeStamp;
    }

    public Route getLastRoute() {
        return mLastRoute;
    }

    public MPLocation getLastRouteOrigin() {
        return mLastRouteOrigin;
    }

    public MPLocation getLastRouteDestination() {
        return mLastRouteDestination;
    }

    public boolean isOriginInsideABuilding() {
        return mIsOriginInsideABuilding;
    }

    public boolean isDestinationInsideABuilding() {
        return mIsDestinationInsideABuilding;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }
}
