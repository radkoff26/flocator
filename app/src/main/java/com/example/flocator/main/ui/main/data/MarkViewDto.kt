package com.example.flocator.main.ui.main.data

import com.example.flocator.main.ui.main.views.mark.MarkView
import com.yandex.mapkit.map.PlacemarkMapObject

data class MarkViewDto(
    val placemark: PlacemarkMapObject,
    val markView: MarkView
)
