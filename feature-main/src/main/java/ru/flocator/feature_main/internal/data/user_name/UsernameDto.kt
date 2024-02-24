package ru.flocator.feature_main.internal.data.user_name

import com.google.gson.annotations.SerializedName

data class UsernameDto(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String
)
