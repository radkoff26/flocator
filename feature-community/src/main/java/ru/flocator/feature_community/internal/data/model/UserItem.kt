package ru.flocator.feature_community.internal.data.model

import com.google.gson.annotations.SerializedName

internal data class UserItem(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("avatarUri")
    val avatarUri: String?
)
