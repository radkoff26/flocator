package ru.flocator.map.internal.data.bitmap_creator

import android.graphics.Bitmap

internal interface BitmapCreator {
    fun createBitmap(): Bitmap
}