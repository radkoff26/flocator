package com.example.flocator.main.utils

import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.ui_view.ViewProvider
import kotlin.math.abs
import kotlin.math.sqrt

class MapUtils {
    companion object {
        fun addViewToMap(
            mapView: MapView,
            viewProvider: ViewProvider,
            point: Point
        ): PlacemarkMapObject {
            return mapView.map.mapObjects.addCollection().addPlacemark(point, viewProvider)
        }

        fun moveWithSpeed(from: Point, to: Point, speed: Double): Point {
            val a = abs(from.latitude - to.latitude)
            val b = abs(from.longitude - to.longitude)
            if (sqrt(a * a + b * b) < speed) {
                return to
            }
            val c = b / a
            var latitude = sqrt(speed * speed / (c * c + 1))
            var longitude = latitude * c
            if (to.latitude < from.latitude) {
                latitude = -latitude
            }
            if (to.longitude < from.longitude) {
                longitude = -longitude
            }
            return Point(from.latitude + latitude, from.longitude + longitude)
        }
    }
}