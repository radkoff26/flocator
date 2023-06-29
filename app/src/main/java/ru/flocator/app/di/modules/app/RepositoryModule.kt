package ru.flocator.app.di.modules.app

import androidx.datastore.core.DataStore
import dagger.Module
import dagger.Provides
import ru.flocator.cache.global.PhotoCacheManager
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_data_store.point.UserLocationPoint
import ru.flocator.core_data_store.user.data.UserCredentials
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.ApplicationDatabase
import ru.flocator.feature_auth.internal.data_source.AuthenticationApi
import ru.flocator.feature_main.internal.data_source.ClientAPI
import ru.flocator.feature_main.internal.data_source.GeocoderAPI
import ru.flocator.feature_settings.internal.data_source.SettingsAPI
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideMainRepository(
        clientAPI: ClientAPI,
        geocoderAPI: GeocoderAPI,
        userApi: ru.flocator.feature_community.internal.data_source.UserApi,
        authenticationApi: AuthenticationApi,
        applicationDatabase: ApplicationDatabase,
        userLocationDataStore: DataStore<UserLocationPoint>,
        userCredentialsStore: DataStore<UserCredentials>,
        userInfoStore: DataStore<UserInfo>,
        photoCacheManager: PhotoCacheManager,
        settingsAPI: SettingsAPI,
        connectionLiveData: ConnectionLiveData
    ): AppRepository {
        return AppRepository(
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