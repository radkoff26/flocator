package ru.flocator.core_dependency.annotations

import ru.flocator.core_dependency.Dependencies
import kotlin.reflect.KClass

annotation class DependenciesKey(val key: KClass<out Dependencies>)
