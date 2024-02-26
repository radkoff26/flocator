package ru.flocator.pager.internal.data

import ru.flocator.core.cache.runtime.data.PhotoState

internal data class Photo(
    val uri: String,
    val state: PhotoState
)