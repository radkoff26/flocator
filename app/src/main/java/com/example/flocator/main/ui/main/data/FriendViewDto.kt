package com.example.flocator.main.ui.main.data

import com.example.flocator.main.ui.main.views.FriendMapView
import com.yandex.mapkit.map.PlacemarkMapObject

data class FriendViewDto(
    val placemark: PlacemarkMapObject,
    val friendMapView: FriendMapView,
    var avatarUri: String?
)
