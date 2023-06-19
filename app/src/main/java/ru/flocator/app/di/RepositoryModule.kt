package ru.flocator.app.di

import androidx.datastore.core.DataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.flocator.cache.global.PhotoCacheManager
import ru.flocator.core_api.api.MainRepository
import ru.flocator.core_client.*
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_data_store.point.UserLocationPoint
import ru.flocator.core_data_store.user.data.UserCredentials
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.ApplicationDatabase

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    fun provideMainRepository(
        clientAPI: ClientAPI,
        geocoderAPI: GeocoderAPI,
        userApi: UserApi,
        authenticationApi: AuthenticationApi,
        applicationDatabase: ApplicationDatabase,
        userLocationDataStore: DataStore<UserLocationPoint>,
        userCredentialsStore: DataStore<UserCredentials>,
        userInfoStore: DataStore<UserInfo>,
        photoCacheManager: PhotoCacheManager,
        settingsAPI: SettingsAPI,
        connectionLiveData: ConnectionLiveData
    ): MainRepository {
        return MainRepository(
            clientAPI,
            geocoderAPI,
            userApi,
            authenticationApi,
            applicationDatabase,
            userLocationDataStore,
            userCredentialsStore,
            userInfoStore,
            photoCacheManager,
            settingsAPI,
            connectionLiveData
        )
    }
}