package ru.flocator.app.main.ui.map.utils

import ru.flocator.app.main.ui.map.domain.map_item.DisposableMapItem

object DisposableMapItemsUtils {
    fun disposeItem(mapItem: DisposableMapItem) {
        mapItem.getDisposables().forEach {
            it?.dispose()
        }
    }
}