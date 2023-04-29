package com.example.flocator.main.models.dto

import com.example.flocator.common.storage.db.entities.Mark
import com.example.flocator.common.storage.db.entities.MarkPhoto
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.google.gson.annotations.SerializedName
import com.yandex.mapkit.geometry.Point

data class MarkDto(

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
    var hasUserLiked: Boolean,
) {
    fun toMarkWithPhotos(): MarkWithPhotos {
        val markPhotos = photos.map {
            MarkPhoto(
                it,
                markId
            )
        }
        return MarkWithPhotos(
            Mark(
                markId,
                authorId,
                location,
                text,
                isPublic,
                place,
                likesCount,
                hasUserLiked
            ),
            markPhotos
        )
    }
}