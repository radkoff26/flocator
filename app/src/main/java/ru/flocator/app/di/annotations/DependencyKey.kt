package ru.flocator.app.di.annotations

import dagger.MapKey
import kotlin.reflect.KClass

@MapKey
annotation class DependencyKey(val key: KClass<*>)
