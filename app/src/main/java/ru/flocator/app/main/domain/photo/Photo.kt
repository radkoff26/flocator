package ru.flocator.app.main.domain.photo

import ru.flocator.cache.runtime.PhotoState

data class Photo(val uri: String, val photoState: PhotoState)
