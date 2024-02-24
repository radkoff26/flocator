package ru.flocator.app.di.modules.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import ru.flocator.data.data_store.info.UserInfo
import ru.flocator.data.data_store.info.UserInfoMediator
import ru.flocator.data.data_store.info.UserInfoSerializer
import ru.flocator.data.data_store.point.UserLocationDataStoreManager
import ru.flocator.data.data_store.point.UserLocationPoint
import ru.flocator.data.data_store.point.UserLocationPointSerializer
import javax.inject.Singleton

@Module
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserLocationDataStore(context: Context): DataStore<UserLocationPoint> =
        DataStoreFactory.create(
            UserLocationPointSerializer(),
            produceFile = { context.dataStoreFile(USER_LOCATION_DATA_STORE_FILE) }
        )

    @Provides
    @Singleton
    fun provideUserLocationDataStoreManager(store: DataStore<UserLocationPoint>): UserLocationDataStoreManager =
        UserLocationDataStoreManager(store)


    @Provides
    @Singleton
    fun provideUserInfoDataStore(context: Context): DataStore<UserInfo> =
        DataStoreFactory.create(
            UserInfoSerializer(),
            produceFile = { context.dataStoreFile(USER_INFO_STORE_FILE) }
        )

    @Provides
    @Singleton
    fun provideUserInfoMediator(retrofit: Retrofit, store: DataStore<UserInfo>): UserInfoMediator =
        UserInfoMediator(store, retrofit)

    private const val USER_LOCATION_DATA_STORE_FILE = "user_location_ds"
    private const val USER_DATA_STORE_FILE = "user_data_ds"
    private const val USER_INFO_STORE_FILE = "user_info_ds"
}