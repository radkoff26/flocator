package ru.flocator.feature_main.internal.data.mark

import com.google.gson.annotations.SerializedName
import ru.flocator.data.database.entities.Mark
import ru.flocator.data.database.entities.MarkPhoto
import ru.flocator.data.database.entities.MarkWithPhotos
import ru.flocator.data.models.location.Coordinates
import java.sql.Timestamp

data class MarkDto(

    @SerializedName("markId")
    val markId: Long,

    @SerializedName("authorId")
    val authorId: Long,

    @SerializedName("point")
    val location: Coordinates,

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

    @SerializedName("createdAt")
    val createdAt: Timestamp
) : java.io.Serializable {
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
                Coordinates(
                    location.latitude,
                    location.longitude
                ),
                text,
                isPublic,
                place,
                likesCount,
                hasUserLiked,
                createdAt
            ),
            markPhotos
        )
    }
}