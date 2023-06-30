package ru.flocator.feature_settings.api.dependencies

import retrofit2.Retrofit
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_dependency.Dependencies

interface SettingsDependencies: Dependencies {
    val appRepository: AppRepository
    val retrofit: Retrofit
}