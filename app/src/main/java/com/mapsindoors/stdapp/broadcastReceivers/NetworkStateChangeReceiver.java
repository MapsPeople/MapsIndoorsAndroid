package com.mapsindoors.stdapp.broadcastReceivers;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;


/**
 * NetworkStateChangeReceiver
 * MapsIndoorsDemo
 * <p>
 * Created by Amine on 17/07/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class NetworkStateChangeReceiver extends BaseBroadcastReceiver {

    public static final String TAG = NetworkStateChangeReceiver.class.getSimpleName();


    public NetworkStateChangeReceiver(Context activityContext) {

        super(activityContext);


    }

    @Override
    public void onReceive(final Context context, final Intent intent) {


        if (intent.getAction().matches(ConnectivityManager.CONNECTIVITY_ACTION)) {
            boolean networkState = MapsIndoorsUtils.isNetworkReachable(mContext);

            reportStateToListeners(networkState);
        }

    }


}