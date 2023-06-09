package ru.flocator.app.main.ui.marks_list.data

import ru.flocator.app.common.storage.db.entities.Mark
import ru.flocator.app.main.data.Photo

data class ListMarkDto(
    val mark: Mark,
    var authorName: String?,
    var photo: Photo,
    var stringifiedDistanceToMark: String,
    val photoCount: Int
)