package ru.flocator.app.main.domain.photo

import ru.flocator.app.common.cache.runtime.PhotoState

data class Photo(val uri: String, val photoState: PhotoState)
