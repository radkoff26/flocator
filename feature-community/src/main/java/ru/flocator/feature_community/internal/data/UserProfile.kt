package ru.flocator.feature_community.internal.data

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

internal data class UserProfile(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("avatarUri")
    val avatarUri: String?,
    @SerializedName("isOnline")
    val isOnline: Boolean,
    @SerializedName("lastOnline")
    val lastOnline: Timestamp?,
    @SerializedName("isFriend")
    val isFriend: Boolean?
)
