package com.example.flocator.main.ui.data.dto

import com.example.flocator.main.models.Mark
import com.example.flocator.main.ui.views.MarkGroupMapView
import com.yandex.mapkit.map.PlacemarkMapObject

data class MarkGroupViewDto(
    val placemark: PlacemarkMapObject,
    val markGroupMapView: MarkGroupMapView,
    val marks: List<Mark>
)
