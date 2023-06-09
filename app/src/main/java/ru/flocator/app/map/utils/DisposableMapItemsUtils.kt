package ru.flocator.app.map.utils

import ru.flocator.app.map.domain.map_item.DisposableMapItem

object DisposableMapItemsUtils {
    fun disposeItem(mapItem: DisposableMapItem) {
        mapItem.getDisposables().forEach {
            it?.dispose()
        }
    }
}