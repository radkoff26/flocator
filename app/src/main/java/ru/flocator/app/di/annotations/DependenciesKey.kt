package ru.flocator.app.di.annotations

import dagger.MapKey
import ru.flocator.core.dependencies.Dependencies
import kotlin.reflect.KClass

@MapKey
annotation class DependenciesKey(val key: KClass<out Dependencies>)
