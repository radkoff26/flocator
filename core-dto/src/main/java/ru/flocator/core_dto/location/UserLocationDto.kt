package ru.flocator.core_dto.location

import com.google.android.gms.maps.model.LatLng

data class UserLocationDto(val userId: Long, val location: LatLng)
