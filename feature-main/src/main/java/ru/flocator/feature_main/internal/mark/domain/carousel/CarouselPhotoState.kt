package ru.flocator.feature_main.internal.mark.domain.carousel

import android.graphics.Bitmap

internal sealed class CarouselPhotoState {
    object Loading : CarouselPhotoState()
    class Loaded(val bitmap: Bitmap) : CarouselPhotoState()
    class Failed(val cause: Throwable) : CarouselPhotoState()
}
