package com.example.flocator.common.storage.storage

import kotlinx.serialization.Serializable

@Serializable
data class UserLocationPoint(val latitude: Double, val longitude: Double)
