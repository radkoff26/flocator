package com.example.flocator.main.ui.map.utils

import com.example.flocator.main.ui.map.domain.map_item.DisposableMapItem

object DisposableMapItemsUtils {
    fun disposeItem(mapItem: DisposableMapItem) {
        mapItem.getDisposables().forEach {
            it?.dispose()
        }
    }
}