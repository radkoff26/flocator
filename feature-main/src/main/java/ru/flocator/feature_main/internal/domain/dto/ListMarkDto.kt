package ru.flocator.feature_main.internal.domain.dto

import ru.flocator.core_database.entities.Mark
import ru.flocator.feature_main.internal.domain.photo.Photo

internal data class ListMarkDto(
    val mark: Mark,
    var authorName: String?,
    var photo: Photo,
    var stringifiedDistanceToMark: String,
    val photoCount: Int
)