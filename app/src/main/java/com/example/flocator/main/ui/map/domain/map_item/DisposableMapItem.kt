package com.example.flocator.main.ui.map.domain.map_item

import io.reactivex.disposables.Disposable

interface DisposableMapItem: MapItem {
    fun getDisposables(): List<Disposable?>
}