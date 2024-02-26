package ru.flocator.feature_main.internal.data.model.mark

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import ru.flocator.data.models.location.Coordinates

data class AddMarkDto(
    @SerializedName("point")
    val location: Coordinates,
    @SerializedName("text")
    val text: String,
    @SerializedName("isPublic")
    val isPublic: Boolean,
    @SerializedName("place")
    val place: String
)
