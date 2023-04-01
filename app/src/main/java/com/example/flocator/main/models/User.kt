package com.example.flocator.main.models

import com.google.gson.annotations.SerializedName
import com.yandex.mapkit.geometry.Point

data class User(
    @SerializedName("id")
    val id: Long,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("location")
    var location: Point,
    @SerializedName("avatarUrl")
    val avatarUrl: String?
)