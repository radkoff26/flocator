package ru.flocator.map.internal.utils

import ru.flocator.map.internal.data.map_item.DisposableMapItem

internal object DisposableMapItemsUtils {
    fun disposeItem(mapItem: DisposableMapItem) {
        mapItem.getDisposables().forEach {
            it?.dispose()
        }
    }
}