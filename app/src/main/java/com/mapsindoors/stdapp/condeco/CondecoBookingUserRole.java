package com.mapsindoors.stdapp.condeco;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public enum CondecoBookingUserRole {

    @SerializedName("1")
    _1(1),
    @SerializedName("2")
    _2(2),
    @SerializedName("3")
    _3(3);
    private final int value;
    private final static Map<Integer, CondecoBookingUserRole> CONSTANTS = new HashMap<>();

    static {
        for (CondecoBookingUserRole c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    CondecoBookingUserRole(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static CondecoBookingUserRole fromValue(int value) {
        CondecoBookingUserRole constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value + ""));
        } else {
            return constant;
        }
    }

}
