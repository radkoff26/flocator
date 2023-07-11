package ru.flocator.feature_settings.api.dependencies

import retrofit2.Retrofit
import ru.flocator.cache.storage.SettingsStorage
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_dependency.Dependencies

interface SettingsDependencies: Dependencies {
    val appRepository: AppRepository
    val connectionLiveData: ConnectionLiveData
    val retrofit: Retrofit
    val settingsStorage: SettingsStorage
}