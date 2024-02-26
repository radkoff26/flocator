package ru.flocator.feature_main.internal.data.model.dto

import ru.flocator.data.database.entities.Mark
import ru.flocator.feature_main.internal.data.model.photo.Photo

internal data class ListMarkDto(
    val mark: Mark,
    var authorName: String?,
    var photo: Photo,
    var stringifiedDistanceToMark: String,
    val photoCount: Int
)