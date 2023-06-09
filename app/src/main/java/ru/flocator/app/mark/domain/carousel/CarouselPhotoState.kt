package ru.flocator.app.mark.domain.carousel

import android.graphics.Bitmap

sealed class CarouselPhotoState {
    object Loading : CarouselPhotoState()
    class Loaded(val bitmap: Bitmap) : CarouselPhotoState()
    class Failed(val cause: Throwable) : CarouselPhotoState()
}
