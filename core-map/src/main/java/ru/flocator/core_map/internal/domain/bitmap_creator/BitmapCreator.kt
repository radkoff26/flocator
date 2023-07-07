package ru.flocator.core_map.internal.domain.bitmap_creator

import android.graphics.Bitmap

internal interface BitmapCreator {
    fun createBitmap(): Bitmap
}