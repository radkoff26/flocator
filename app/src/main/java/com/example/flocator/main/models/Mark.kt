package com.example.flocator.main.models

import com.yandex.mapkit.geometry.Point

data class Mark(val markId: Long, val authorId: Long, val imageList: List<String>, val location: Point)
