package com.example.flocator.main.ui.main.data.friend

import com.example.flocator.main.ui.main.views.friend.UserView
import com.yandex.mapkit.map.PlacemarkMapObject

data class FriendViewDto(
    val placemark: PlacemarkMapObject,
    val userView: UserView
)
