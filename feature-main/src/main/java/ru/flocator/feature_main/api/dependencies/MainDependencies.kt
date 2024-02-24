package ru.flocator.feature_main.api.dependencies

import retrofit2.Retrofit
import ru.flocator.core.cache.global.PhotoLoader
import ru.flocator.core.connection.live_data.ConnectionLiveData
import ru.flocator.core.dependencies.Dependencies
import ru.flocator.data.data_store.credentials.UserCredentialsDataStoreManager
import ru.flocator.data.data_store.info.UserInfoDataStoreManager
import ru.flocator.data.data_store.point.UserLocationDataStoreManager
import ru.flocator.data.preferences.LanguagePreferences
import ru.flocator.map.api.MapPreferences

interface MainDependencies : Dependencies {
    val connectionLiveData: ConnectionLiveData
    val locationDataStoreManager: UserLocationDataStoreManager
    val infoDataStoreManager: UserInfoDataStoreManager
    val credentialsDataStoreManager: UserCredentialsDataStoreManager
    val retrofit: Retrofit
    val languagePreferences: LanguagePreferences
    val mapPreferences: MapPreferences
    val photoLoader: PhotoLoader
}
