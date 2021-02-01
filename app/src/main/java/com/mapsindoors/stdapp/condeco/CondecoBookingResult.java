package com.mapsindoors.stdapp.condeco;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CondecoBookingResult
{
    @SerializedName("bookings")
    @Expose
    private List<CondecoBooking> bookings = null;
    private String externalId;

    public List<CondecoBooking> getBookings() {
        return bookings;
    }

    public void setBookings(List<CondecoBooking> bookings) {
        this.bookings = bookings;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
