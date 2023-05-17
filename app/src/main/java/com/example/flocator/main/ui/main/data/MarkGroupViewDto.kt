package com.example.flocator.main.ui.main.data

import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.main.ui.main.views.mark_group.MarkGroupView
import com.yandex.mapkit.map.PlacemarkMapObject

data class MarkGroupViewDto(
    val placemark: PlacemarkMapObject,
    val markGroupView: MarkGroupView,
    val marks: List<MarkWithPhotos>
)
