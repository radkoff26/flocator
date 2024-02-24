package ru.flocator.map.internal.domain.comparing

internal interface CompareCallback<O, N> {
    fun areComparedItemsTheSame(item1: O, item2: N): Boolean
    fun areComparedItemsContentsTheSame(item1: O, item2: N): Boolean
}
