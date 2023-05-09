package com.example.flocator.common.storage.store.point

import kotlinx.serialization.Serializable

@Serializable
data class UserLocationPoint(val latitude: Double, val longitude: Double) {
    companion object {
        val DEFAULT = UserLocationPoint(0.0, 0.0)
    }
}
