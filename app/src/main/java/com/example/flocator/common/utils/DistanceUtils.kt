package com.example.flocator.common.utils

import com.yandex.mapkit.geometry.Point
import kotlin.math.pow
import kotlin.math.sqrt

object DistanceUtils {
    private const val METER = 0.000009468

    fun distanceBetweenToString(point1: Point, point2: Point): String {
        val distance = calculateDistance(point1, point2)
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

    private fun calculateDistance(point1: Point, point2: Point): Double {
        val latitudeDelta = (point1.latitude - point2.latitude)
        val longitudeDelta = (point1.longitude - point2.longitude)
        return sqrt(latitudeDelta.pow(2) + longitudeDelta.pow(2))
    }
}