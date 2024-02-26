package ru.flocator.feature_settings.internal.core.di

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
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import ru.flocator.feature_settings.internal.data.data_source.SettingsDataSource
import ru.flocator.feature_settings.internal.core.di.annotations.FragmentScope
import ru.flocator.feature_settings.internal.ui.view_models.*
import ru.flocator.feature_settings.internal.ui.view_models.BlackListViewModel
import ru.flocator.feature_settings.internal.ui.view_models.ChangePasswordViewModel
import ru.flocator.feature_settings.internal.ui.view_models.DeleteAccountViewModel
import ru.flocator.feature_settings.internal.ui.view_models.PrivacyViewModel
import ru.flocator.feature_settings.internal.ui.view_models.SettingsViewModel

@Module
internal abstract class SettingsModule {

    companion object {
        @Provides
        @FragmentScope
        fun provideSettingsDataSource(dependencies: SettingsDependencies): SettingsDataSource =
            dependencies.retrofit.create()

        @Provides
        @FragmentScope
        fun provideViewModelFactory(map: ViewModelsMap): ViewModelProvider.Factory =
            ViewModelFactory(map)
    }

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(impl: SettingsViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(BlackListViewModel::class)
    abstract fun bindBlackListViewModel(impl: BlackListViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(PrivacyViewModel::class)
    abstract fun bindPrivacyViewModel(impl: PrivacyViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(ChangePasswordViewModel::class)
    abstract fun bindChangePasswordViewModel(impl: ChangePasswordViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(key = DeleteAccountViewModel::class)
    abstract fun bindDeleteAccountViewModel(impl: DeleteAccountViewModel): ViewModel
}