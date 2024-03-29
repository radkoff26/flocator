package ru.flocator.map.internal.data.difference

import ru.flocator.map.internal.data.comparing.CompareCallback
import ru.flocator.map.internal.data.map_item.MapItem

internal object MapItemsDifferenceCalculator {
    fun <O : MapItem, N> calculateDifference(
        previousList: List<O>,
        newList: List<N>,
        compareCallback: CompareCallback<O, N>
    ): Difference<O, N> {
        val addedItems = ArrayList<N>()
        val updatedItems = ArrayList<O>()
        val removedItems = ArrayList<O>()
        val checked = BooleanArray(newList.size) { false }
        previousList.forEach {
            val indexOfIt = newList.indexOfFirst { item ->
                compareCallback.areComparedItemsTheSame(it, item)
            }
            if (indexOfIt != -1) {
                checked[indexOfIt] = true
                if (!compareCallback.areComparedItemsContentsTheSame(it, newList[indexOfIt])) {
                    updatedItems.add(it)
                }
            } else {
                removedItems.add(it)
            }
        }
        newList.forEachIndexed { index, item ->
            if (!checked[index]) {
                addedItems.add(item)
            }
        }
        return Difference(addedItems, updatedItems, removedItems)
    }
}