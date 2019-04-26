package com.mapsindoors.stdapp.models;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mapsindoors.mapssdk.RouteCoordinate;

/**
 * Created by amine on 11/09/2017.
 */
public class UIRouteNavigation
{
	// Values set when processing the route
	public int legIndex, stepIndex, bgViewIndex;
	public boolean isIndoors, alignWithPathEntryPoints;
	public RouteCoordinate p0, p1;
	public LatLngBounds latLngBounds;


	// Cached values
	// float[] pntsXformed;
	/**
	 * AABB
	 */
	public LatLngBounds bbox;

	/**
	 * AABB center
	 */
	public LatLng bboxCenter;

	/**
	 * LatLngBounds of the OBB (oriented bounding box). Only set if alignWithPathEntryPoints is true
	 */
	public LatLngBounds obb;

	/**
	 * OBB's center. Only set if alignWithPathEntryPoints is true
	 */
	public LatLng obbCenter;

	/**
	 * Calculated bearing for the current leg/step (only valid if alignWithPathEntryPoints is true)
	 */
	public float targetBearing;

	/**
	 * Somewhat hacky now, holds two guys, one per tilt value ...
	 */
	public CameraPosition.Builder[] gmapCameraPositionBuilder;


	public UIRouteNavigation( RouteCoordinate p0, RouteCoordinate p1, LatLngBounds latLngBounds, boolean alignWithPathEntryPoints, float targetBearing ) {
		this.p0 = p0;
		this.p1 = p1;
		this.latLngBounds = latLngBounds;
		this.alignWithPathEntryPoints = alignWithPathEntryPoints;
		this.targetBearing = targetBearing;
		this.bbox = null;
		this.bboxCenter = null;
		this.obb = null;
		this.obbCenter = null;
		this.gmapCameraPositionBuilder = new CameraPosition.Builder[2];
	}

	public void set( boolean isIndoors, int legIndex, int stepIndex, int bgViewIndex ) {
		this.legIndex = legIndex;
		this.stepIndex = stepIndex;
		this.bgViewIndex = bgViewIndex;
		this.isIndoors = isIndoors;
	}

	public void set( boolean isIndoors, int legIndex, int bgViewIndex ) {
		this.legIndex = legIndex;
		this.stepIndex = -1;
		this.bgViewIndex = bgViewIndex;
		this.isIndoors = isIndoors;
	}
}