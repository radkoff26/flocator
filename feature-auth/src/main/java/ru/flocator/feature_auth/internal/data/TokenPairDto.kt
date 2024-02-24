package ru.flocator.feature_auth.internal.data

import com.google.gson.annotations.SerializedName

data class TokenPairDto(
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("accessToken")
    val accessToken: String
)