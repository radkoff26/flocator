package ru.flocator.feature_settings.internal.di

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import retrofit2.create
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_dependency.findDependencies
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import ru.flocator.feature_settings.internal.data_source.SettingsAPI
import ru.flocator.feature_settings.internal.di.annotations.FragmentScope

@Module
internal object SettingsModule {

    @Provides
    @FragmentScope
    fun provideNavController(fragment: Fragment): NavController =
        fragment.findNavController()

    @Provides
    @FragmentScope
    fun provideSettingsDependencies(fragment: Fragment): SettingsDependencies =
        fragment.findDependencies()

    @Provides
    @FragmentScope
    fun provideSettingsAPI(dependencies: SettingsDependencies): SettingsAPI =
        dependencies.retrofit.create()
}