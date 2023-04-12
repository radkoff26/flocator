package com.example.flocator.main.ui.main.data

import com.example.flocator.main.models.Mark
import com.example.flocator.main.ui.main.views.MarkGroupMapView
import com.yandex.mapkit.map.PlacemarkMapObject

data class MarkGroupViewDto(
    val placemark: PlacemarkMapObject,
    val markGroupMapView: MarkGroupMapView,
    val marks: List<Mark>
)
