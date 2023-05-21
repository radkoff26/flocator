package com.example.flocator.common.storage.db.entities

import androidx.room.Embedded
import androidx.room.Relation
import com.example.flocator.main.models.dto.MarkDto
import com.example.flocator.main.ui.main.data.PointDto

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
            PointDto(
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
