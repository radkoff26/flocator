package ru.flocator.feature_main.internal.add_mark.di

import dagger.Component
import ru.flocator.app.main.internal.add_mark.ui.AddMarkFragment
import ru.flocator.core_api.api.AppRepository

@Component(dependencies = [AppRepository::class])
internal interface AddMarkFragmentComponent {

    @Component.Factory
    interface Factory {
        fun create(repository: AppRepository): AddMarkFragmentComponent
    }

    fun inject(fragment: AddMarkFragment)
}