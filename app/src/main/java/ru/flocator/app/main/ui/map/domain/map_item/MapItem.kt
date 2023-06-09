package ru.flocator.app.main.ui.map.domain.map_item

import ru.flocator.app.main.ui.map.ui.BitmapCreator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

interface MapItem {
    fun getItemMarker(): Marker?
    fun getBitmapCreator(): BitmapCreator
    fun getLocation(): LatLng
    fun setItemMarker(marker: Marker?)
}