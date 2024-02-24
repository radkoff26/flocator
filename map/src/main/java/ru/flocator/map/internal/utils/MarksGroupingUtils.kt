package ru.flocator.map.internal.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import ru.flocator.data.models.location.Coordinates
import ru.flocator.map.api.entity.MapMark
import ru.flocator.map.internal.domain.entity.MarkGroup
import ru.flocator.map.internal.extensions.toCoordinates
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

internal object MarksGroupingUtils {

    fun groupMarks(
        marks: List<MapMark>,
        visibleRegion: VisibleRegion,
        mapWidth: Float,
        markWidth: Float
    ): List<MarkGroup> {
        val distanceBetweenEdges = getDistanceBetweenPoints(
            visibleRegion.nearLeft.toCoordinates(),
            visibleRegion.nearRight.toCoordinates()
        )
        val boundDistance: Double = markWidth * distanceBetweenEdges / mapWidth
        val map: Map<Long, MapMark> = buildMap {
            marks.forEach {
                put(it.markId, it)
            }
        }
        val graph: Map<Long, Set<Long>> = buildMap {
            var current = 0
            while (current < marks.size) {
                val currentMark = marks[current]
                val set: MutableSet<Long> = mutableSetOf()
                var index = 0
                while (index < marks.size) {
                    if (current == index) {
                        index++
                        continue
                    }
                    val distance = getDistanceBetweenPoints(
                        currentMark.location,
                        marks[index].location
                    )
                    if (distance < boundDistance) {
                        set.add(marks[index].markId)
                    }
                    index++
                }
                put(currentMark.markId, set)
                current++
            }
        }
        return bfsGrouping(map, graph)
    }

    private fun bfsGrouping(map: Map<Long, MapMark>, graph: Map<Long, Set<Long>>): List<MarkGroup> {
        val notVisited: MutableSet<Long> = buildSet {
            map.forEach { (key, _) ->
                add(key)
            }
        }.toMutableSet()
        val markGroups: MutableList<MarkGroup> = ArrayList()
        while (notVisited.isNotEmpty()) {
            val first = notVisited.first()
            val markList = mutableListOf<MapMark>()
            val queue: Queue<Long> = LinkedList()
            queue.add(first)
            while (queue.isNotEmpty()) {
                val current = queue.poll()!!
                if (!notVisited.contains(current)) {
                    continue
                }
                notVisited.remove(current)
                markList.add(map[current]!!)
                val vertices = graph[current]!!
                vertices.forEach {
                    queue.add(it)
                }
            }
            markGroups.add(MarkGroup(markList, getCenterOfGroup(markList)))
        }
        return markGroups
    }

    private fun getCenterOfGroup(group: List<MapMark>): LatLng {
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

    private fun getDistanceBetweenPoints(coordinates1: Coordinates, coordinates2: Coordinates): Double {
        return sqrt(
            (coordinates1.latitude - coordinates2.latitude).pow(2.0) +
                    (coordinates1.longitude - coordinates2.longitude).pow(2.0)
        )
    }
}