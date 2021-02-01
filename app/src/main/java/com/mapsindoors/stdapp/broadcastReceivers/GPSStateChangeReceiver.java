package com.mapsindoors.stdapp.broadcastReceivers;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;


/**
 * GPSStateChangeReceiver
 * MapsIndoorsDemo
 * <p>
 * Created by Mohammed Amine Naimi on 03/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class GPSStateChangeReceiver extends BaseBroadcastReceiver {
    public static final String TAG = GPSStateChangeReceiver.class.getSimpleName();


    public GPSStateChangeReceiver(Context activityContext) {
        super(activityContext);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String intentAction = intent.getAction();

        if ((intentAction != null) && intentAction.matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {

            boolean gpsState = PSUtils.isLocationServiceEnabled(mContext);

            reportStateToListeners(gpsState);

        }
    }


}