package ru.flocator.core_map.internal.domain.map_item

import com.google.android.gms.maps.model.LatLng
import ru.flocator.core_map.internal.domain.bitmap_creator.BitmapCreator

internal interface MapItem {

    fun getItemMarkerId(): String

    fun getBitmapCreator(): BitmapCreator

    fun getLocation(): LatLng
}