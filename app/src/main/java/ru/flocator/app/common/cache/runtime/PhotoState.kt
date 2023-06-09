package ru.flocator.app.common.cache.runtime

import android.graphics.Bitmap

sealed class PhotoState {
    object Loading: PhotoState()
    class Loaded(val bitmap: Bitmap): PhotoState()
    class Failed(val throwable: Throwable): PhotoState()
}
