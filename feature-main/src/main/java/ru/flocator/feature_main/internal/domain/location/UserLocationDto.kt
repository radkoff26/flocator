package ru.flocator.feature_main.internal.domain.location

import com.google.android.gms.maps.model.LatLng

data class UserLocationDto(val userId: Long, val location: LatLng)
