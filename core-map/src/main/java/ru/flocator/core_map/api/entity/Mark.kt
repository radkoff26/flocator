package ru.flocator.core_map.api.entity

import com.google.android.gms.maps.model.LatLng

data class Mark(
    val markId: Long,
    val authorId: Long,
    val location: LatLng,
    val thumbnailUri: String?,
    val authorAvatarUri: String?
)