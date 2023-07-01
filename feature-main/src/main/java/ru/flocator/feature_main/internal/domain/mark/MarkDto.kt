package ru.flocator.feature_main.internal.domain.mark

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import ru.flocator.feature_main.internal.domain.location.LatLngDto
import ru.flocator.core_database.entities.Mark
import ru.flocator.core_database.entities.MarkPhoto
import ru.flocator.core_database.entities.MarkWithPhotos
import java.sql.Timestamp

data class MarkDto(

    @SerializedName("markId")
    val markId: Long,

    @SerializedName("authorId")
    val authorId: Long,

    @SerializedName("point")
    val location: LatLngDto,

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
                LatLng(
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