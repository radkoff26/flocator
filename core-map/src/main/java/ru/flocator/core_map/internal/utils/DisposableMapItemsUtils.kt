package ru.flocator.core_map.internal.utils

import ru.flocator.core_map.internal.domain.map_item.DisposableMapItem

internal object DisposableMapItemsUtils {
    fun disposeItem(mapItem: DisposableMapItem) {
        mapItem.getDisposables().forEach {
            it?.dispose()
        }
    }
}