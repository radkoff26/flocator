package ru.flocator.app.di.modules.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import ru.flocator.core_data_store.point.UserLocationPoint
import ru.flocator.core_data_store.point.UserLocationPointSerializer
import ru.flocator.core_data_store.user.data.UserCredentials
import ru.flocator.core_data_store.user.data.UserDataSerializer
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_data_store.user.info.UserInfoSerializer
import javax.inject.Singleton

@Module
object DataStoreModule {

    @Provides
    @Singleton
    fun locationDataStore(context: Context): DataStore<UserLocationPoint> =
        DataStoreFactory.create(
            UserLocationPointSerializer(),
            produceFile = { context.dataStoreFile(USER_LOCATION_DATA_STORE_FILE) }
        )

    @Provides
    @Singleton
    fun userDataStore(context: Context): DataStore<UserCredentials> =
        DataStoreFactory.create(
            UserDataSerializer(),
            produceFile = { context.dataStoreFile(USER_DATA_STORE_FILE) }
        )

    @Provides
    @Singleton
    fun userInfoDataStore(context: Context): DataStore<UserInfo> =
        DataStoreFactory.create(
            UserInfoSerializer(),
            produceFile = { context.dataStoreFile(USER_INFO_STORE_FILE) }
        )

    private const val ENCRYPTED_PREFS = "encrypted_prefs"
    private const val USER_LOCATION_DATA_STORE_FILE = "user_location_ds"
    private const val USER_DATA_STORE_FILE = "user_data_ds"
    private const val USER_INFO_STORE_FILE = "user_info_ds"
}