package com.mapsindoors.stdapp.condeco;

import com.google.gson.annotations.SerializedName;

public class CondecoTokenResult {
    @SerializedName("access_token")
    private String access_token;
    @SerializedName("token_type")
    private String token_type;
    @SerializedName("expires_in")
    private int expires_in;

    public String getAccessToken() {
        return access_token;
    }

    public String getTokenType() {
        return token_type;
    }

    public int getExpiresIn() {
        return expires_in;
    }
}
