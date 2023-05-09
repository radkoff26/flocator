package com.example.flocator.community.data_classes

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Long?,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("avatarUrl")
    val avatarUrl: String?,

    @SerializedName("isOnline")
    var isOnline: Boolean?,

    @SerializedName("lastOnline")
    var lastOnline: String?,

    @SerializedName("friendRequests")
    var friendRequests: ArrayList<FriendRequests> = arrayListOf(),

    @SerializedName("friends")
    var friends: ArrayList<Friends> = arrayListOf()
)
