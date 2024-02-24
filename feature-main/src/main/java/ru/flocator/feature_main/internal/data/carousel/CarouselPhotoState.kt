package ru.flocator.feature_main.internal.data.carousel

import android.graphics.Bitmap

internal sealed class CarouselPhotoState {
    object Loading : CarouselPhotoState()
    class Loaded(val bitmap: Bitmap) : CarouselPhotoState()
    class Failed(val cause: Throwable) : CarouselPhotoState()
}
