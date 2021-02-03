package com.mapsindoors.stdapp.positionprovider.ciscoDna;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;

public class CiscoDNAEntry implements PositionResult {

    @SerializedName("tennantId")
    private String mTennantId;

    @SerializedName("deviceId")
    private String mDeviceId;

    @SerializedName("macAddress")
    private String mMacAddress;

    @SerializedName("latitude")
    private double mLatitude;

    @SerializedName("longitude")
    private double mLongitude;

    @SerializedName("datasetId")
    private String mDatasetId;

    @SerializedName("venueId")
    private String mVenueId;

    @SerializedName("buildingId")
    private String mBuildingId;

    @SerializedName("floorIndex")
    private String mFloorIndex;

    @SerializedName("timestamp")
    private String mTimestamp;

    @SerializedName("operatingSystem")
    private String mOperatingSystem;

    @SerializedName("confidenceFactor")
    private int mConfidenceFactor;

    @SerializedName("maxDetectedRssi")
    private int mMaxDetectedRssi;

    @SerializedName("type")
    private String mType;


    @Nullable
    @Override
    public Point getPoint() {
        if (hasFloor()) {
            return new Point(mLatitude, mLongitude, getFloor());
        }else {
            return new Point(mLatitude, mLongitude);
        }
    }

    @Override
    public boolean hasFloor() {
        if (mFloorIndex != null){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getFloor() {
        return Integer.parseInt(mFloorIndex);
    }

    @Override
    public void setFloor(int i) { }

    @Override
    public boolean hasBearing() {
        return false;
    }

    @Override
    public float getBearing() {
        return 0;
    }

    @Override
    public void setBearing(float v) { }

    @Override
    public boolean hasAccuracy() {
        return true;
    }

    @Override
    public float getAccuracy() {
        /*
        The CiscoDNA "confidence factor" is the width of their "bounding box"
        (much like an accuracy circle, but a box, for some reason...).
        The user's position is centered in this square box, so we compute the distance
        to the square box's corner (using pythagoras theorem).
        Lastly, we need to convert from feet to meters (1 foot = 0.3048 meters)
         */
        int x = Math.round((float) mConfidenceFactor/2);
        float acc = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(x, 2));
        return acc * 0.3048f;
    }

    @Override
    public void setAccuracy(float v) { }

    @Nullable
    @Override
    public PositionProvider getProvider() {
        return null;
    }

    @Override
    public void setProvider(@Nullable PositionProvider positionProvider) { }

    @Nullable
    @Override
    public Location getAndroidLocation() {
        Location loc = new Location("");
        loc.setLatitude(mLatitude);
        loc.setLongitude(mLongitude);
        loc.setAccuracy(getAccuracy());
        loc.setTime(System.currentTimeMillis());
        return loc;
    }

    @Override
    public void setAndroidLocation(@Nullable Location location) { }

    public String getTimestamp() {
        return mTimestamp;
    }

    public String getDatasetId() {
        return mDatasetId;
    }

    public String getVenueId() {
        return mVenueId;
    }

    public String getBuildingId() {
        return mBuildingId;
    }

    public String getTennantId() {
        return mTennantId;
    }

    public int getRssi() {
        return mMaxDetectedRssi;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    @NonNull
    @Override
    public String toString() {
        String str = "Rssi: " + mMaxDetectedRssi + "\n" +
                "Confidence: " + mConfidenceFactor + "\n" +
                "Timestamp: " + mTimestamp + "\n" +
                "Building: " + mBuildingId + "\n" +
                "Venue: " + mVenueId + "\n" +
                "TennantId: " + mTennantId + "\n";
        return str;
    }
}
