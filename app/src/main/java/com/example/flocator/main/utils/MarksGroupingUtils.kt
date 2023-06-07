package com.example.flocator.main.utils

import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.main.ui.main.data.MarkGroup
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import kotlin.math.pow
import kotlin.math.sqrt


object MarksGroupingUtils {

    fun groupMarks(
        marks: List<MarkWithPhotos>,
        visibleRegion: VisibleRegion,
        mapWidth: Float,
        markWidth: Float
    ): List<MarkGroup> {
        val distanceBetweenEdges = getDistanceBetweenPoints(
            visibleRegion.nearLeft,
            visibleRegion.nearRight
        )
        val boundDistance: Double = markWidth * distanceBetweenEdges / mapWidth
        val mutablePoints: MutableList<MarkWithPhotos> = ArrayList(marks)
        val markGroups: MutableList<MarkGroup> = ArrayList()
        val current = 0
        while (current < mutablePoints.size) {
            var i = current + 1
            val currentList: MutableList<MarkWithPhotos> = ArrayList()
            val latLng: LatLng = mutablePoints[current].mark.location
            currentList.add(mutablePoints[current])
            while (i < mutablePoints.size) {
                val distance: Double =
                    getDistanceBetweenPoints(latLng, mutablePoints[i].mark.location)
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

    private fun getCenterOfGroup(group: List<MarkWithPhotos>): LatLng {
        var minLatitude = 85.0
        var minLongitude = 180.0
        var maxLatitude = -85.0
        var maxLongitude = -180.0
        for (mark in group) {
            val point = mark.mark.location
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