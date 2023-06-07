package com.example.flocator.main.ui.main.views.map

import com.example.flocator.main.ui.main.views.BitmapCreator

class Difference<T : MapItem> internal constructor(
    private val addedItems: List<T>,
    private val updatedItems: List<T>,
    private val removedItems: List<T>
) {
    fun dispatchDifferenceTo(
        map: FLocatorMapFragment,
        bitmapCreatorProvider: (obj: T) -> BitmapCreator,
        onRemoveMapItemCallback: ((obj: T) -> Unit)? = null
    ) {
        addedItems.forEach {
            map.drawMapItemOnMap(
                it,
                bitmapCreatorProvider.invoke(it)
            )
        }
        updatedItems.forEach(map::updateMapItemOnMap)
        if (onRemoveMapItemCallback != null) {
            removedItems.forEach {
                map.removeMapItemFromMap(
                    it,
                    onRemoveMapItemCallback
                )
            }
        } else {
            removedItems.forEach(map::removeMapItemFromMap)
        }
    }
}