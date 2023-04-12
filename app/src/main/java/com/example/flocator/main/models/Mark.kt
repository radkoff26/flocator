package com.example.flocator.main.models

import com.google.android.gms.common.internal.Objects
import com.google.gson.annotations.SerializedName
import com.yandex.mapkit.geometry.Point

data class Mark(
    @SerializedName("markId")
    val markId: Long,
    @SerializedName("authorId")
    val authorId: Long,
    @SerializedName("point")
    val location: Point,
    @SerializedName("text")
    val text: String,
    @SerializedName("isPublic")
    val isPublic: Boolean,
    @SerializedName("photos")
    val photos: List<String>,
    @SerializedName("place")
    val place: String,
    @SerializedName("likesCount")
    var likesCount: Int,
    @SerializedName("hasUserLiked")
    var hasUserLiked: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mark

        if (markId != other.markId) return false
        if (authorId != other.authorId) return false
        if (location.latitude != other.location.latitude || location.longitude != other.location.longitude) return false
        if (text != other.text) return false
        if (isPublic != other.isPublic) return false
        if (photos != other.photos) return false
        if (place != other.place) return false

        return true
    }

    override fun hashCode(): Int {
        var result = markId.hashCode()
        result = 31 * result + authorId.hashCode()
        result = 31 * result + Objects.hashCode(location)
        result = 31 * result + text.hashCode()
        result = 31 * result + isPublic.hashCode()
        result = 31 * result + photos.hashCode()
        result = 31 * result + place.hashCode()
        return result
    }
}
