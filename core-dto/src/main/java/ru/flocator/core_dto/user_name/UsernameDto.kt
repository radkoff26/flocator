package ru.flocator.core_dto.user_name

import com.google.gson.annotations.SerializedName

data class UsernameDto(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String
)
