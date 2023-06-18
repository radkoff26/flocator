package ru.flocator.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import ru.flocator.core_data_store.point.UserLocationPoint
import ru.flocator.core_data_store.point.UserLocationPointSerializer
import ru.flocator.core_data_store.user.data.UserCredentials
import ru.flocator.core_data_store.user.data.UserDataSerializer
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_data_store.user.info.UserInfoSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SharedStorageModule {
    private const val ENCRYPTED_PREFS = "encrypted_prefs"
    private const val USER_LOCATION_DATA_STORE_FILE = "user_location_ds"
    private const val USER_DATA_STORE_FILE = "user_data_ds"
    private const val USER_INFO_STORE_FILE = "user_info_ds"

    @Provides
    @Singleton
    fun encryptedSharedPreferences(@ApplicationContext context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    @Provides
    @Singleton
    fun locationDataStore(@ApplicationContext context: Context): DataStore<ru.flocator.core_data_store.point.UserLocationPoint> =
        DataStoreFactory.create(
            ru.flocator.core_data_store.point.UserLocationPointSerializer(),
            produceFile = { context.dataStoreFile(USER_LOCATION_DATA_STORE_FILE) }
        )

    @Provides
    @Singleton
    fun userDataStore(@ApplicationContext context: Context): DataStore<ru.flocator.core_data_store.user.data.UserCredentials> =
        DataStoreFactory.create(
            ru.flocator.core_data_store.user.data.UserDataSerializer(),
            produceFile = { context.dataStoreFile(USER_DATA_STORE_FILE) }
        )

    @Provides
    @Singleton
    fun userInfoDataStore(@ApplicationContext context: Context): DataStore<ru.flocator.core_data_store.user.info.UserInfo> =
        DataStoreFactory.create(
            ru.flocator.core_data_store.user.info.UserInfoSerializer(),
            produceFile = { context.dataStoreFile(USER_INFO_STORE_FILE) }
        )
}