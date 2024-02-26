package ru.flocator.core.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Provider

typealias ViewModelsMap = Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>

/**
 * [ViewModelFactory] is necessary for creating and storing ViewModels.
 * Note that it uses Dagger Provider mechanism.
 * */
class ViewModelFactory constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator: Provider<out ViewModel> = creators[modelClass]
            ?: throw IllegalArgumentException("unknown model class $modelClass")
        try {
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}