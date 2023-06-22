package ru.flocator.core_photo_pager.internal.domain.photo

import ru.flocator.cache.runtime.PhotoState

internal data class Photo(
    val uri: String,
    val state: PhotoState
)