package ru.flocator.feature_settings.internal.data.privacy

import com.google.gson.annotations.SerializedName

data class PrivacyData(
    @SerializedName("userId")
    val id: Long,
    @SerializedName("status")
    val status: PrivacyType
)