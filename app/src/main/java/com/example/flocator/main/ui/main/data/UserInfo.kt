package com.example.flocator.main.ui.main.data

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("login")
    val login: String,
    @SerializedName("avatarUrl")
    val avatarUri: String?
)
