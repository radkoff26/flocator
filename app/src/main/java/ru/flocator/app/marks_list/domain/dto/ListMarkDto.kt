package ru.flocator.app.marks_list.domain.dto

import ru.flocator.app.common.storage.db.entities.Mark
import ru.flocator.app.main.domain.photo.Photo

data class ListMarkDto(
    val mark: Mark,
    var authorName: String?,
    var photo: Photo,
    var stringifiedDistanceToMark: String,
    val photoCount: Int
)