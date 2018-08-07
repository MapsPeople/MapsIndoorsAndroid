package com.mapsindoors.stdapp.apis.googleplaces;

/**
 * PlacesServiceStatus
 * MISDKAND
 * <p>
 * Created by Jose J Varó on 9/21/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class PlacesServiceStatus
{
	/** This request was invalid */
	public static final String INVALID_REQUEST = "INVALID_REQUEST";

	/** The response contains a valid result */
	public static final String OK = "OK";

	/** The application has gone over its request quota */
	public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";

	/** The application is not allowed to use the PlacesService */
	public static final String REQUEST_DENIED = "REQUEST_DENIED";

	/** The PlacesService request could not be processed due to a server error. The request may succeed if you try again */
	public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

	/** No result was found for this request */
	public static final String ZERO_RESULTS = "ZERO_RESULTS";
}
