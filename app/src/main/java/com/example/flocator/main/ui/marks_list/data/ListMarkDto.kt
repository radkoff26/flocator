package com.example.flocator.main.ui.marks_list.data

import com.example.flocator.common.storage.db.entities.Mark
import com.example.flocator.main.data.Photo

data class ListMarkDto(
    val mark: Mark,
    var authorName: String?,
    var photo: Photo,
    var stringifiedDistanceToMark: String,
    val photoCount: Int
)