package ru.flocator.core_map.internal.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import ru.flocator.core_map.api.entity.Mark
import ru.flocator.core_map.internal.domain.entity.MarkGroup
import kotlin.math.pow
import kotlin.math.sqrt

internal object MarksGroupingUtils {

    fun groupMarks(
        marks: List<Mark>,
        visibleRegion: VisibleRegion,
        mapWidth: Float,
        markWidth: Float
    ): List<MarkGroup> {
        val distanceBetweenEdges = getDistanceBetweenPoints(
            visibleRegion.nearLeft,
            visibleRegion.nearRight
        )
        val boundDistance: Double = markWidth * distanceBetweenEdges / mapWidth
        val mutablePoints: MutableList<Mark> = ArrayList(marks)
        val markGroups: MutableList<MarkGroup> = ArrayList()
        val current = 0
        while (current < mutablePoints.size) {
            var i = current + 1
            val currentList: MutableList<Mark> = ArrayList()
            val latLng: LatLng = mutablePoints[current].location
            currentList.add(mutablePoints[current])
            while (i < mutablePoints.size) {
                val distance: Double =
                    getDistanceBetweenPoints(latLng, mutablePoints[i].location)
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

    private fun getCenterOfGroup(group: List<Mark>): LatLng {
        var minLatitude = 85.0
        var minLongitude = 180.0
        var maxLatitude = -85.0
        var maxLongitude = -180.0
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
        return LatLng((minLatitude + maxLatitude) / 2, (minLongitude + maxLongitude) / 2)
    }

    private fun getDistanceBetweenPoints(latLng1: LatLng, latLng2: LatLng): Double {
        return sqrt(
            (latLng1.latitude - latLng2.latitude).pow(2.0) + (latLng1.longitude - latLng2.longitude).pow(
                2.0
            )
        )
    }
}