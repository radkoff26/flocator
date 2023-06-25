package ru.flocator.core_map.domain.map_item

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import ru.flocator.core_map.ui.BitmapCreator

interface MapItem {
    fun getItemMarker(): Marker?
    fun getBitmapCreator(): BitmapCreator
    fun getLocation(): LatLng
    fun setItemMarker(marker: Marker?)
}