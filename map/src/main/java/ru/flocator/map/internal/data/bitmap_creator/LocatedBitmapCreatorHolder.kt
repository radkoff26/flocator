package ru.flocator.map.internal.data.bitmap_creator

import com.google.android.gms.maps.model.LatLng

internal interface LocatedBitmapCreatorHolder {

    fun getHolderLocation(): LatLng

    fun getHolderBitmapCreator(): BitmapCreator
}