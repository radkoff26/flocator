package com.example.flocator.main.ui.main.views.map

import com.example.flocator.main.ui.main.views.BitmapCreator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

interface MapItem {
    fun getItemMarker(): Marker?
    fun getBitmapCreator(): BitmapCreator
    fun getLocation(): LatLng
    fun setItemMarker(marker: Marker?)
}