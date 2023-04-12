package com.example.flocator.main.ui.mark.data

import com.google.gson.annotations.SerializedName

data class UserNameDto(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String
)
