package ru.flocator.core.view_model.annotations

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

@MapKey
annotation class ViewModelKey(val key: KClass<out ViewModel>)
