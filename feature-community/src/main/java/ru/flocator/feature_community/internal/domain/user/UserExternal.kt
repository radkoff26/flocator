package ru.flocator.feature_community.internal.domain.user

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class UserExternal(
    @SerializedName("userId")
    var userId: Long?,
    @SerializedName("firstName")
    var firstName: String?,
    @SerializedName("lastName")
    var lastName: String?,
    @SerializedName("avatarUri")
    var avatarUri: String?,
    @SerializedName("isOnline")
    var isOnline: Boolean?,
    @SerializedName("lastOnline")
    var lastOnline: Timestamp?,
    @SerializedName("friends")
    var friends: ArrayList<UserExternalFriends> = arrayListOf(),
    @SerializedName("isBlockedByUser")
    var isBlockedByUser: Boolean?,
    @SerializedName("hasBlockedUser")
    var hasBlockedUser: Boolean?,
    @SerializedName("hasUserRequestedFriendship")
    var hasUserRequestedFriendship: Boolean?,
    @SerializedName("hasTargetUserRequestedFriendship")
    var hasTargetUserRequestedFriendship: Boolean?,
    @SerializedName("isFriend")
    var isFriend: Boolean?

)
