package ru.flocator.core_map.internal.domain.view

internal interface ReusableView {
    var isBusy: Boolean

    fun use() {
        isBusy = true
    }

    fun recycle() {
        isBusy = false
    }
}