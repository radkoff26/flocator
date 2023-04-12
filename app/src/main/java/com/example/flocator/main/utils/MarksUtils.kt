package com.example.flocator.main.utils

import com.example.flocator.main.models.Mark
import com.example.flocator.main.ui.main.data.MarkGroup
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import kotlin.math.pow
import kotlin.math.sqrt


object MarksUtils {

    fun groupMarks(marks: List<Mark>, visibleRegion: VisibleRegion, mapWidth: Float, markWidth: Float): List<MarkGroup> {
        val distanceBetweenEdges = getDistanceBetweenPoints(visibleRegion.bottomLeft, visibleRegion.bottomRight)
        val boundDistance: Double = markWidth * distanceBetweenEdges / mapWidth
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
        var minLatitude = 86.0
        var minLongitude = 151.0
        var maxLatitude = -86.0
        var maxLongitude = -211.0
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