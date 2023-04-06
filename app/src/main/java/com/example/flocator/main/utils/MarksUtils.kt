package com.example.flocator.main.utils

import com.example.flocator.main.models.Mark
import com.example.flocator.main.ui.data.MarkGroup
import com.yandex.mapkit.geometry.Point
import kotlin.math.pow
import kotlin.math.sqrt


object MarksUtils {
    private const val DISTANCE_BETWEEN_ON_MAX_SCALE = 0.000005
    private const val MAX_SCALE = 21f

    fun groupMarks(marks: List<Mark>, scale: Float): List<MarkGroup> {
        val boundDistance: Double = if (scale == MAX_SCALE) {
            DISTANCE_BETWEEN_ON_MAX_SCALE
        } else {
            DISTANCE_BETWEEN_ON_MAX_SCALE * 400 * MAX_SCALE / scale
        }
        val mutablePoints: MutableList<Mark> = ArrayList(marks)
        val markGroups: MutableList<MarkGroup> = ArrayList()
        val current = 0
        while (current < mutablePoints.size) {
            var i = current + 1
            val currentList: MutableList<Mark> = ArrayList()
            val point: Point = mutablePoints[current].location
            currentList.add(mutablePoints[current])
            while (i < mutablePoints.size) {
                val distance: Double = getDistanceBetweenPoints(point, mutablePoints[i].location)
                if (distance < boundDistance) {
                    currentList.add(mutablePoints[i])
                    mutablePoints.removeAt(i)
                } else {
                    i++
                }
            }
            markGroups.add(MarkGroup(currentList, getCenterOfGroup(currentList)))
            mutablePoints.removeAt(current)
        }
        return markGroups
    }

    private fun getCenterOfGroup(group: List<Mark>): Point {
        var minLatitude = 91.0
        var minLongitude = 181.0
        var maxLatitude = -1.0
        var maxLongitude = -1.0
        for (mark in group) {
            val point = mark.location
            if (point.latitude > maxLatitude) {
                maxLatitude = point.latitude
            }
            if (point.latitude < minLatitude) {
                minLatitude = point.latitude
            }
            if (point.longitude > maxLongitude) {
                maxLongitude = point.longitude
            }
            if (point.longitude < minLongitude) {
                minLongitude = point.longitude
            }
        }
        return Point((minLatitude + maxLatitude) / 2, (minLongitude + maxLongitude) / 2)
    }

    private fun getDistanceBetweenPoints(point1: Point, point2: Point): Double {
        return sqrt(
            (point1.latitude - point2.latitude).pow(2.0) + (point1.longitude - point2.longitude).pow(
                2.0
            )
        )
    }
}