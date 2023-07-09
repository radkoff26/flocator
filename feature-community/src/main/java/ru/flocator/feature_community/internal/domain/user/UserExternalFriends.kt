package ru.flocator.feature_community.internal.domain.user

import com.google.gson.annotations.SerializedName

data class UserExternalFriends(
    @SerializedName("userId")
    var userId: Long?,
    @SerializedName("firstName")
    var firstName: String?,
    @SerializedName("lastName")
    var lastName: String?,
    @SerializedName("avatarUri")
    var avatarUri: String?,
    @SerializedName("isMutual")
    var isMutual: Boolean?
)