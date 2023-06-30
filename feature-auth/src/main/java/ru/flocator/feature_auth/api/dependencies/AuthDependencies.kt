package ru.flocator.feature_auth.api.dependencies

import retrofit2.Retrofit
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_dependency.Dependencies

interface AuthDependencies: Dependencies {
    val appRepository: AppRepository
    val retrofit: Retrofit
}