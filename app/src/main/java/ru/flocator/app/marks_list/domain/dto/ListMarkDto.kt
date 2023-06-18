package ru.flocator.app.marks_list.domain.dto

import ru.flocator.app.main.domain.photo.Photo
import ru.flocator.core_database.entities.Mark

data class ListMarkDto(
    val mark: Mark,
    var authorName: String?,
    var photo: Photo,
    var stringifiedDistanceToMark: String,
    val photoCount: Int
)