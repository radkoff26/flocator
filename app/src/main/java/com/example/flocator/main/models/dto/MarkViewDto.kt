package com.example.flocator.main.models.dto

import com.example.flocator.main.ui.views.MarkMapView
import com.yandex.mapkit.map.PlacemarkMapObject

data class MarkViewDto(
    val placemark: PlacemarkMapObject,
    val markMapView: MarkMapView,
    var thumbnailUri: String?,
    var avatarUrl: String?
)
