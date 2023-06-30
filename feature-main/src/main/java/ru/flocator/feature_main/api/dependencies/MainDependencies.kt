package ru.flocator.feature_main.api.dependencies

import retrofit2.Retrofit
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_dependency.Dependencies

interface MainDependencies: Dependencies {
    val connectionLiveData: ConnectionLiveData
    val appRepository: AppRepository
    val retrofit: Retrofit
}
