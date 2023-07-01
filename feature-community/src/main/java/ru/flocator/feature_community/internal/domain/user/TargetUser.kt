package ru.flocator.feature_community.internal.domain.user

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class TargetUser(
    @SerializedName("id")
    val id: Long?,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("avatarUri")
    val avatarUri: String?,

    @SerializedName("isOnline")
    var isOnline: Boolean?,

    @SerializedName("lastOnline")
    var lastOnline: Timestamp?,

    @SerializedName("friendRequests")
    var friendRequests: ArrayList<FriendRequests> = arrayListOf(),

    @SerializedName("friends")
    var friends: ArrayList<Friends> = arrayListOf()
)
