package com.example.flocator.settings.data_models;

import com.google.gson.annotations.SerializedName

data class PrivacyData(
    @SerializedName("userId")
    val id: Long,
    @SerializedName("status")
    val status: String
)