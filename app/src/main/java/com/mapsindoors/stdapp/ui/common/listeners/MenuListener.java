package com.mapsindoors.stdapp.ui.common.listeners;

import com.mapsindoors.mapssdk.MPLocation;

/**
 * Listener interface to catch data context fetching events
 *
 * @author Martin Hansen
 */
public interface MenuListener {
	/**
	 * If called, the callee want to show a route to a location on the map
	 */
	/**
	 * If called, the callee want to change venue
	 */
	void onMenuVenueSelect(String venueId);
}
