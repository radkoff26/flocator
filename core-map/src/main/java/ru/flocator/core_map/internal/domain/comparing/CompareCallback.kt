package ru.flocator.core_map.internal.domain.comparing

internal interface CompareCallback<T> {
    fun areComparedItemsTheSame(item1: T, item2: T): Boolean
    fun areComparedItemsContentsTheSame(item1: T, item2: T): Boolean
}
