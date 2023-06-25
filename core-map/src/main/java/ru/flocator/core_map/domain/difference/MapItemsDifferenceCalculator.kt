package ru.flocator.core_map.domain.difference

import ru.flocator.core_map.domain.comparing.CompareCallback
import ru.flocator.core_map.domain.map_item.MapItem

object MapItemsDifferenceCalculator {
    fun <T: MapItem> calculateDifference(
        previousList: List<T>,
        newList: List<T>,
        compareCallback: CompareCallback<T>
    ): Difference<T> {
        val addedItems = ArrayList<T>()
        val updatedItems = ArrayList<T>()
        val removedItems = ArrayList<T>()
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