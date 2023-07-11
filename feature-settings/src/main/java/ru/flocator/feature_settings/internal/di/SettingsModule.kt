package ru.flocator.feature_settings.internal.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.create
import ru.flocator.core_view_model.ViewModelFactory
import ru.flocator.core_view_model.ViewModelsMap
import ru.flocator.core_view_model.annotations.ViewModelKey
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import ru.flocator.feature_settings.internal.data_source.SettingsAPI
import ru.flocator.feature_settings.internal.di.annotations.FragmentScope
import ru.flocator.feature_settings.internal.view_models.SettingsFragmentViewModel

@Module
internal abstract class SettingsModule {

    companion object {
        @Provides
        @FragmentScope
        fun provideSettingsAPI(dependencies: SettingsDependencies): SettingsAPI =
            dependencies.retrofit.create()

        @Provides
        @FragmentScope
        fun provideViewModelFactory(map: ViewModelsMap): ViewModelProvider.Factory =
            ViewModelFactory(map)
    }

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(SettingsFragmentViewModel::class)
    abstract fun bindSettingsViewModel(impl: SettingsFragmentViewModel): ViewModel
}