package com.mapsindoors.stdapp.condeco;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public enum CondecoBookingStatus {

    @SerializedName("0")
    _0(0),
    @SerializedName("1")
    _1(1),
    @SerializedName("2")
    _2(2),
    @SerializedName("3")
    _3(3),
    @SerializedName("4")
    _4(4),
    @SerializedName("5")
    _5(5),
    @SerializedName("6")
    _6(6);
    private final int value;
    private final static Map<Integer, CondecoBookingStatus> CONSTANTS = new HashMap<>();

    static {
        for (CondecoBookingStatus c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    CondecoBookingStatus(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static CondecoBookingStatus fromValue(int value) {
        CondecoBookingStatus constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value + ""));
        } else {
            return constant;
        }
    }

}
