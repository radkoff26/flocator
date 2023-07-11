package ru.flocator.core_map.api.entity

import com.google.android.gms.maps.model.LatLng

data class User(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val location: LatLng,
    val avatarUri: String?
)