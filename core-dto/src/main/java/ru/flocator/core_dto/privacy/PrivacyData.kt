package ru.flocator.core_dto.privacy;

import com.google.gson.annotations.SerializedName

data class PrivacyData(
    @SerializedName("userId")
    val id: Long,
    @SerializedName("status")
    val status: String
)