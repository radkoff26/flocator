package ru.flocator.data.models.auth

import com.google.gson.annotations.SerializedName

data class UserCredentialsDto(
    @SerializedName("username")
    val login: String,
    @SerializedName("password")
    val password: String
)
