package com.example.flocator.main.ui.main.data

import com.example.flocator.main.ui.main.views.FriendView
import com.yandex.mapkit.map.PlacemarkMapObject

data class FriendViewDto(
    val placemark: PlacemarkMapObject,
    val friendView: FriendView,
    var avatarUri: String?
)
