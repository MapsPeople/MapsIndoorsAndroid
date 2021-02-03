package com.mapsindoors.stdapp.listeners;


import androidx.annotation.Nullable;

/**
 * GenericObjectResultCallback
 * MISDKAND
 * <p>
 * Created by Amine on 15/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public interface GenericObjectResultCallback<T>
{
	void onResultReady( @Nullable T text );
}
