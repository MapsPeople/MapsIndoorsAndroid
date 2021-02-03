package com.mapsindoors.stdapp.ui.components.mapfloorselector;

import androidx.annotation.NonNull;

import com.mapsindoors.mapssdk.Floor;

interface MapFloorSelectorAdapterListener {
    void onFloorSelectionChanged(@NonNull Floor newFloor);
}
