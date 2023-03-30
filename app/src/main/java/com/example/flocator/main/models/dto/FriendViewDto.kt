package com.example.flocator.main.models.dto

import com.example.flocator.main.ui.views.FriendMapView
import com.yandex.mapkit.map.PlacemarkMapObject

data class FriendViewDto(
    val placemark: PlacemarkMapObject,
    val friendMapView: FriendMapView,
    var avatarUri: String?
)
