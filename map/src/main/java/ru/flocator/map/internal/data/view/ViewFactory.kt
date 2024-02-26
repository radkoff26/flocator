package ru.flocator.map.internal.data.view

internal interface ViewFactory<T: ReusableView> {

    fun create(onRecycle: () -> Unit): T

    fun onClear()
}
