package com.example.flocator.main.models.dto

import com.yandex.mapkit.geometry.Point

data class UserLocationDto(val userId: Long, val location: Point)
