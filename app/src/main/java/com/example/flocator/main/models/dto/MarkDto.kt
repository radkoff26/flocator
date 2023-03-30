package com.example.flocator.main.models.dto

import com.google.gson.annotations.SerializedName
import com.yandex.mapkit.geometry.Point
import okhttp3.MultipartBody

data class MarkDto(
    @SerializedName("authorId")
    val authorId: Long,
    @SerializedName("point")
    val location: Point,
    @SerializedName("text")
    val text: String,
    @SerializedName("isPublic")
    val isPublic: Boolean
)
