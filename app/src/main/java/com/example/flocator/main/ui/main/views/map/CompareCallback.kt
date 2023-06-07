package com.example.flocator.main.ui.main.views.map

interface CompareCallback<T> {
    fun areComparedItemsTheSame(item1: T, item2: T): Boolean
    fun areComparedItemsContentsTheSame(item1: T, item2: T): Boolean
}
