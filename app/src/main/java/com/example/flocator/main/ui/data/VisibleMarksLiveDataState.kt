package com.example.flocator.main.ui.data

data class VisibleMarksLiveDataState(
    val minLatitude: Double,
    val maxLatitude: Double,
    val minLongitude: Double,
    val maxLongitude: Double,
    val markGroups: List<MarkGroup>
)
