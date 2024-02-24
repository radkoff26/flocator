package ru.flocator.map.api.entity

import ru.flocator.data.models.location.Coordinates

data class MapMark(
    val markId: Long,
    val authorId: Long,
    val location: Coordinates,
    val thumbnailUri: String?,
    val authorAvatarUri: String?
)