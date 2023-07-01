package ru.flocator.feature_settings.internal.di

import dagger.Module
import dagger.Provides
import retrofit2.create
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import ru.flocator.feature_settings.internal.data_source.SettingsAPI
import ru.flocator.feature_settings.internal.di.annotations.FragmentScope

@Module
internal object SettingsModule {

    @Provides
    @FragmentScope
    fun provideSettingsAPI(dependencies: SettingsDependencies): SettingsAPI =
        dependencies.retrofit.create()
}