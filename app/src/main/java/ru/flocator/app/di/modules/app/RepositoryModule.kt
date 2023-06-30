package ru.flocator.app.di.modules.app

import androidx.datastore.core.DataStore
import dagger.Module
import dagger.Provides
import ru.flocator.cache.global.PhotoCacheManager
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_data_store.point.UserLocationPoint
import ru.flocator.core_data_store.user.data.UserCredentials
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.ApplicationDatabase
import javax.inject.Singleton

@Module(
    includes = [
        DataStoreModule::class,
        DatabaseModule::class,
        CacheModule::class
    ]
)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMainRepository(
        applicationDatabase: ApplicationDatabase,
        userLocationDataStore: DataStore<UserLocationPoint>,
        userCredentialsStore: DataStore<UserCredentials>,
        userInfoStore: DataStore<UserInfo>,
        photoCacheManager: PhotoCacheManager
    ): AppRepository {
        return AppRepository(
            applicationDatabase,
            userLocationDataStore,
            userCredentialsStore,
            userInfoStore,
            photoCacheManager
        )
    }
}