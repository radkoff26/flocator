package com.example.flocator.main.ui.add_mark.data

import com.google.gson.annotations.SerializedName
import com.yandex.mapkit.geometry.Point

data class AddMarkDto(
    @SerializedName("authorId")
    var authorId: Long,
    @SerializedName("point")
    val location: Point,
    @SerializedName("text")
    val text: String,
    @SerializedName("isPublic")
    val isPublic: Boolean,
    @SerializedName("place")
    val place: String
)
