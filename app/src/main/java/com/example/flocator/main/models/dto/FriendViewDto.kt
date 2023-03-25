package com.example.flocator.main.models.dto

import com.example.flocator.main.views.FriendMapView
import com.yandex.mapkit.map.PlacemarkMapObject

data class FriendViewDto(val placemark: PlacemarkMapObject, val friendMapView: FriendMapView, var avatarUrl: String?)
