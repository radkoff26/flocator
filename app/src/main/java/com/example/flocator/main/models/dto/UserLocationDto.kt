package com.example.flocator.main.models.dto

import com.google.android.gms.maps.model.LatLng

data class UserLocationDto(val userId: Long, val location: LatLng)
