package ru.flocator.core_map.internal.ui

import android.graphics.Bitmap

internal interface BitmapCreator {
    fun createBitmap(): Bitmap
}