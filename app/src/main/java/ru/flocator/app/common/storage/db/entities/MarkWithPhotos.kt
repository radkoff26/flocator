package ru.flocator.app.common.storage.db.entities

import androidx.room.Embedded
import androidx.room.Relation
import ru.flocator.app.common.dto.mark.MarkDto
import ru.flocator.app.common.dto.location.LatLngDto

data class MarkWithPhotos(
    @Embedded
    val mark: Mark,

    @Relation(parentColumn = "markId", entityColumn = "markId")
    val photos: List<MarkPhoto>
) {
    fun toMarkDto(): MarkDto {
        val photos = photos.map(MarkPhoto::uri)
        return MarkDto(
            mark.markId,
            mark.authorId,
            LatLngDto(
                mark.location.latitude,
                mark.location.longitude
            ),
            mark.text,
            mark.isPublic,
            photos,
            mark.place,
            mark.likesCount,
            mark.hasUserLiked,
            mark.createdAt
        )
    }
}
