package ru.flocator.app.settings.domain.privacy;

import com.google.gson.annotations.SerializedName;

public enum PrivacyState {
    @SerializedName("fixed")
    FIXED("fixed"),
    @SerializedName("precise")
    PRECISE("precise");

    PrivacyState(String fixed) {

    }
}
