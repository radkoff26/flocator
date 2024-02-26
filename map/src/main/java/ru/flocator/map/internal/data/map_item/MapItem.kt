package ru.flocator.map.internal.data.map_item

import com.google.android.gms.maps.model.LatLng
import ru.flocator.map.internal.data.bitmap_creator.BitmapCreator

internal interface MapItem {

    fun getItemMarkerId(): String

    fun getBitmapCreator(): BitmapCreator

    fun getLocation(): LatLng
}