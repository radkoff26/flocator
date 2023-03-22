package com.example.flocator.main.utils

import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.ui_view.ViewProvider

class MapUtils {
    companion object {
        fun addViewToMap(
            mapView: MapView,
            viewProvider: ViewProvider,
            point: Point
        ): PlacemarkMapObject {
            return mapView.map.mapObjects.addCollection().addPlacemark(point, viewProvider)
        }
    }
}