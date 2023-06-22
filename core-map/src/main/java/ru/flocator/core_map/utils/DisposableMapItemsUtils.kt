package ru.flocator.core_map.utils

import ru.flocator.core_map.domain.map_item.DisposableMapItem

object DisposableMapItemsUtils {
    fun disposeItem(mapItem: DisposableMapItem) {
        mapItem.getDisposables().forEach {
            it?.dispose()
        }
    }
}