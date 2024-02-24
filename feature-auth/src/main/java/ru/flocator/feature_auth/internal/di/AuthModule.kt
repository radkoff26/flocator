package ru.flocator.feature_auth.internal.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.create
import ru.flocator.core.view_model.ViewModelFactory
import ru.flocator.core.view_model.ViewModelsMap
import ru.flocator.core.view_model.annotations.ViewModelKey
import ru.flocator.feature_auth.api.dependencies.AuthDependencies
import ru.flocator.feature_auth.internal.data_source.AuthDataSource
import ru.flocator.feature_auth.internal.di.annotations.FragmentScope
import ru.flocator.feature_auth.internal.view_models.RegistrationViewModel

@Module
internal abstract class AuthModule {

    companion object {
        @Provides
        @FragmentScope
        fun provideAuthDataStore(dependencies: AuthDependencies): AuthDataSource =
            dependencies.retrofit.create()

        @Provides
        @FragmentScope
        fun provideViewModelFactory(map: ViewModelsMap): ViewModelProvider.Factory =
            ViewModelFactory(map)
    }

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(RegistrationViewModel::class)
    abstract fun bindRegistrationViewModel(impl: RegistrationViewModel): ViewModel
}
