package com.example.flocator.main.ui.main.views.map

import io.reactivex.disposables.Disposable

interface DisposableMapItem: MapItem {
    fun getDisposables(): List<Disposable?>
}