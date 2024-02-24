package ru.flocator.map.internal.domain.view

internal interface ViewFactory<T: ReusableView> {

    fun create(onRecycle: () -> Unit): T

    fun onClear()
}
