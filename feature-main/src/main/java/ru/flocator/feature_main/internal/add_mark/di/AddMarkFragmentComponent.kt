package ru.flocator.feature_main.internal.add_mark.di

import dagger.Component
import ru.flocator.core_api.api.AppRepository
import ru.flocator.feature_main.internal.add_mark.ui.AddMarkFragment

@Component(dependencies = [AppRepository::class])
internal interface AddMarkFragmentComponent {

    @Component.Factory
    interface Factory {
        fun create(repository: AppRepository): AddMarkFragmentComponent
    }

    fun inject(fragment: AddMarkFragment)
}