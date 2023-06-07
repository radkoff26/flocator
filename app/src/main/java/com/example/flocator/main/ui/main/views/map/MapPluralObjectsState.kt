package com.example.flocator.main.ui.main.views.map

import java.util.function.Predicate

typealias ObjectUpdateMapper<T> = (obj: T) -> T

interface MapPluralObjectsState<T> {
    fun addObject(obj: T)
    fun removeObject(predicate: Predicate<T>)
    fun updateObject(predicate: Predicate<T>, mapper: ObjectUpdateMapper<T>)
    fun onClearAll()
}

interface MapPluralIdentifiableObjectsState<T, ID>: MapPluralObjectsState<T> {
    fun removeObjectById(id: ID)
}