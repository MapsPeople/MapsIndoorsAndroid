package com.mapsindoors.stdapp.models;

import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.Route;

public class LastRouteInfo
{
	private Route mLastRoute;
	private Location mLastRouteOrigin;
	private Location mLastRouteDestination;
	private boolean mIsOriginInsideABuilding;
	private boolean mIsDestinationInsideABuilding;
	private long mTimeStamp;

	public LastRouteInfo( Route lastRoute, Location lastRouteOrigin, Location lastRouteDestination, boolean isOriginInsideABuilding, boolean isDestinationInsideABuilding, long timeStamp ) {
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

	public Location getLastRouteOrigin() {
		return mLastRouteOrigin;
	}

	public Location getLastRouteDestination() {
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
