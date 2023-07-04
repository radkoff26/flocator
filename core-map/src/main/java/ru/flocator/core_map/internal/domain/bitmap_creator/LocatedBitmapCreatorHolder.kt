package ru.flocator.core_map.internal.domain.bitmap_creator

import com.google.android.gms.maps.model.LatLng

internal interface LocatedBitmapCreatorHolder {

    fun getHolderLocation(): LatLng

    fun getHolderBitmapCreator(): BitmapCreator
}