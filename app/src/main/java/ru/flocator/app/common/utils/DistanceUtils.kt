package ru.flocator.app.common.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.pow
import kotlin.math.sqrt

object DistanceUtils {
    private const val METER = 0.000009468

    fun distanceBetweenToString(latLng1: LatLng, latLng2: LatLng): String {
        val distance = calculateDistance(latLng1, latLng2)
        val meters = distance / METER
        return if (meters >= 1000) {
            // Getting integer part of kilometers
            val kilometers = (meters / 1000).toInt()
            // Getting integer part of the 100-th of meters
            val rest = ((meters % 1000) / 100).toInt()
            "$kilometers.${rest}км"
        } else {
            "${meters.toInt()}м"
        }
    }

    private fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Double {
        val latitudeDelta = (latLng1.latitude - latLng2.latitude)
        val longitudeDelta = (latLng1.longitude - latLng2.longitude)
        return sqrt(latitudeDelta.pow(2) + longitudeDelta.pow(2))
    }
}