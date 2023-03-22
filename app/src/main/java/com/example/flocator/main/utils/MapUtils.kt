package com.example.flocator.main.utils

import android.view.View
import android.widget.Toast
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.ui_view.ViewProvider

class MapUtils {
    companion object {
        fun addViewToMap(mapView: MapView, viewProvider:ViewProvider, point: Point): PlacemarkMapObject {
            val mark = mapView.map.mapObjects.addCollection().addPlacemark(point, viewProvider)
            mark.addTapListener { mapObject, point ->
                Toast.makeText(mapView.context, "QWERTY", Toast.LENGTH_LONG).show()
                true
            }
            return mark
        }
    }
}