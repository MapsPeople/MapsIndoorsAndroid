package com.mapsindoors.stdapp.positionprovider;

import android.content.Context;
import androidx.annotation.NonNull;

import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.stdapp.positionprovider.gps.GoogleAPIPositionProvider;

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

	}

	@NonNull
	public List<PositionProvider> getPositionProviders() {
		return mPositionProviders;
	}
}
