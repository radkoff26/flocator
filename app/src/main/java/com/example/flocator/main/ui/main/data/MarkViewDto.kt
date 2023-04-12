package com.example.flocator.main.ui.main.data

import com.example.flocator.main.ui.main.views.MarkMapView
import com.yandex.mapkit.map.PlacemarkMapObject

data class MarkViewDto(
    val placemark: PlacemarkMapObject,
    val markMapView: MarkMapView,
    var thumbnailUri: String?,
    var avatarUrl: String?
)
