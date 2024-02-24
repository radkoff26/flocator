package ru.flocator.core.cache.runtime.data

import android.graphics.Bitmap

sealed class PhotoState {
    object Loading: PhotoState()
    class Loaded(val bitmap: Bitmap): PhotoState()
    class Failed(val throwable: Throwable): PhotoState()
}
