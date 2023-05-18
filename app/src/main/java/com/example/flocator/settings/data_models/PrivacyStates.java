package com.example.flocator.settings.data_models;

import com.google.gson.annotations.SerializedName;

public enum PrivacyStates {
    @SerializedName("fixed")
    FIXED("fixed"),
    @SerializedName("precise")
    PRECISE("precise");

    PrivacyStates(String fixed) {

    }
}
