package ru.flocator.app.map.domain.map_item

import io.reactivex.disposables.Disposable

interface DisposableMapItem: MapItem {
    fun getDisposables(): List<Disposable?>
}