package com.mapsindoors.stdapp.broadcastReceivers;

import android.content.Context;
import android.content.Intent;

import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;

public class BluetoothStateChangeReceiver extends BaseBroadcastReceiver {




    public BluetoothStateChangeReceiver(Context activityContext )
    {
        super(activityContext);

    }


    @Override
    public void onReceive(Context context, Intent intent) {

       boolean bluetoothState =  MapsIndoorsUtils.isBluetoothEnabled();

       reportStateToListeners(bluetoothState);
    }
}
