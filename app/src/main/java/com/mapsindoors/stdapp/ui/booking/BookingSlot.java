package com.mapsindoors.stdapp.ui.booking;

import androidx.annotation.Nullable;

import com.mapsindoors.mapssdk.MPBooking;

import java.util.Date;

public class BookingSlot {

    private final Date mFrom;
    private final Date mTo;

    private MPBooking mLocalBooking;
    private MPBooking mRemoteBooking;

    private final OnBookingClickedListener mClickedListener;

    public BookingSlot(Date from, Date to, OnBookingClickedListener listener){
        mClickedListener = listener;
        mFrom = from;
        mTo = to;
    }

    public void setLocalBooking(MPBooking booking){
        mLocalBooking = booking;
    }

    public void setRemoteBooking(MPBooking booking){
        mRemoteBooking = booking;
    }

    public boolean isBooked(){
        return mLocalBooking != null || mRemoteBooking != null;
    }

    public Date getStart(){
        return mFrom;
    }

    public Date getEnd(){
        return mTo;
    }

    public OnBookingClickedListener getOnBookClickedListener(){
        return mClickedListener;
    }

    public MPBooking getLocalBooking(){
        return mLocalBooking;
    }

    public MPBooking getRemoteBooking(){
        return mRemoteBooking;
    }

    public boolean isOnlyBookedOnRemote(){
        return mRemoteBooking != null && mLocalBooking == null;
    }

    public boolean isManaged(){
        return (mLocalBooking != null && mLocalBooking.getIsManaged()) ||
                (mRemoteBooking != null && mRemoteBooking.getIsManaged());
    }

}
