package ru.flocator.core_dto.mark

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import ru.flocator.core_dto.location.LatLngDto
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
    fun toMarkWithPhotos(): ru.flocator.core_database.entities.MarkWithPhotos {
        val markPhotos = photos.map {
            ru.flocator.core_database.entities.MarkPhoto(
                it,
                markId
            )
        }
        return ru.flocator.core_database.entities.MarkWithPhotos(
            ru.flocator.core_database.entities.Mark(
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