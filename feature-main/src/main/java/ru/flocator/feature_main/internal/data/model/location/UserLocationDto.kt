package ru.flocator.feature_main.internal.data.model.location

import ru.flocator.data.models.location.Coordinates

data class UserLocationDto(val userId: Long, val location: Coordinates)
