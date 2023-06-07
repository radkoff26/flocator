package com.example.flocator.main.ui.main.views.map

object DisposableMapItemsUtils {

    fun disposeItem(mapItem: DisposableMapItem) {
        mapItem.getDisposables().forEach {
            it?.dispose()
        }
    }
}