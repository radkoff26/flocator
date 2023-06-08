package com.example.flocator.main.ui.map.domain.comparing

interface CompareCallback<T> {
    fun areComparedItemsTheSame(item1: T, item2: T): Boolean
    fun areComparedItemsContentsTheSame(item1: T, item2: T): Boolean
}
