package ru.flocator.feature_settings.internal.data.model.privacy

import com.google.gson.annotations.SerializedName

internal data class PrivacyData(
    @SerializedName("userId")
    val id: Long,
    @SerializedName("status")
    val status: PrivacyType
)