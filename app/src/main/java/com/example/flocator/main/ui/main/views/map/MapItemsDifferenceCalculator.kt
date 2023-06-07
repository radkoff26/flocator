package com.example.flocator.main.ui.main.views.map

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