package ru.flocator.feature_main.internal.domain.photo

import ru.flocator.cache.runtime.PhotoState

internal data class Photo(val uri: String, val photoState: PhotoState)
