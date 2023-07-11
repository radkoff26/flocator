package ru.flocator.core_map.internal.domain.difference

import ru.flocator.core_map.internal.domain.map_item.MapItem

internal class Difference<O : MapItem, N> internal constructor(
    private val addedItems: List<N>,
    private val updatedItems: List<O>,
    private val removedItems: List<O>
) {
    inline fun dispatchDifferenceTo(
        onAddMapItemCallback: (obj: N) -> Unit,
        onUpdateMapItemCallback: (obj: O) -> Unit,
        onRemoveMapItemCallback: (obj: O) -> Unit
    ) {
        addedItems.forEach {
            onAddMapItemCallback.invoke(it)
        }
        updatedItems.forEach {
            onUpdateMapItemCallback.invoke(it)
        }
        removedItems.forEach {
            onRemoveMapItemCallback.invoke(it)
        }
    }
}