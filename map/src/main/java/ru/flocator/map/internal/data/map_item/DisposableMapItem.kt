package ru.flocator.map.internal.data.map_item

import io.reactivex.disposables.Disposable

internal interface DisposableMapItem: MapItem {

    fun getDisposables(): List<Disposable?>
}