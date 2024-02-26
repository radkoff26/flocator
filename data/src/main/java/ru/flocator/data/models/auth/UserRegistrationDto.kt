package ru.flocator.data.models.auth

import com.google.gson.annotations.SerializedName

data class UserRegistrationDto(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("login")
    val login: String,
    @SerializedName("password")
    val password: String
)
