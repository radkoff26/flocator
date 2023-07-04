package ru.flocator.core_map.internal.domain.view

internal interface ViewFactory<T: ReusableView> {

    fun create(onRecycle: () -> Unit): T

    fun onClear()
}
