package ru.flocator.core_map.internal.domain.map_item

import io.reactivex.disposables.Disposable

internal interface DisposableMapItem: MapItem {
    fun getDisposables(): List<Disposable?>
}