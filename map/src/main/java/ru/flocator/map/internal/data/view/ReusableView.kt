package ru.flocator.map.internal.data.view

internal interface ReusableView {
    var isBusy: Boolean

    fun use() {
        isBusy = true
    }

    fun recycle() {
        isBusy = false
    }
}