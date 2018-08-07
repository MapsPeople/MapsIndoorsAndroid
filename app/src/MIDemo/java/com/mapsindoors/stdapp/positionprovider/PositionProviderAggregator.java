package com.mapsindoors.stdapp.positionprovider;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.positionprovider.gpsPositionProvider.GoogleAPIPositionProvider;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author Jose J Varó - Copyright © 2018 MapsPeople A/S. All rights reserved.
 */
public class PositionProviderAggregator
{
	private final List<PositionProvider> mPositionProviders;

	public PositionProviderAggregator( @NonNull Context context )
	{
		mPositionProviders = new ArrayList<>();

		mPositionProviders.add( new GoogleAPIPositionProvider( context ) );
	/*	BeaconPositionProvider beaconPosProvider = new BeaconPositionProvider(
				context,
				context.getResources().getString( R.string.mapsindoors_api_key ),
				"",
				"m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24",
				false
		);
		mPositionProviders.add(beaconPosProvider);*/


		//mPositionProviders.add( new MixedPositionProviderV1.InternalPosProvider(
		//
		//		new CiscoPositionProvider( context, "DA - URL!!!" ), CiscoPositionProvider.class )
		//);

		//positionProviders.add( new BeaconPositionProvider( context, null ) );
	}

	@NonNull
	public List<PositionProvider> getPositionProviders() {
		return mPositionProviders;
	}
}
