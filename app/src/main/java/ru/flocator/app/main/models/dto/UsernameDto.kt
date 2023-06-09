package ru.flocator.app.main.models.dto

import com.google.gson.annotations.SerializedName

data class UsernameDto(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String
)
