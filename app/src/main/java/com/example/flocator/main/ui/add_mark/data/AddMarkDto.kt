package com.example.flocator.main.ui.add_mark.data

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class AddMarkDto(
    @SerializedName("authorId")
    var authorId: Long,
    @SerializedName("point")
    val location: LatLng,
    @SerializedName("text")
    val text: String,
    @SerializedName("isPublic")
    val isPublic: Boolean,
    @SerializedName("place")
    val place: String
)
