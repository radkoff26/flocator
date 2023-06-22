package ru.flocator.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.flocator.app.di.annotations.DependencyKey
import ru.flocator.core_data_store.point.UserLocationPoint
import ru.flocator.core_data_store.point.UserLocationPointSerializer
import ru.flocator.core_data_store.store.DataStorage
import ru.flocator.core_data_store.user.data.UserCredentials
import ru.flocator.core_data_store.user.data.UserDataSerializer
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_data_store.user.info.UserInfoSerializer
import javax.inject.Singleton

@Module
@Singleton
class SharedStorageModule {

    @Provides
    @Singleton
    @IntoMap
    @DependencyKey(EncryptedSharedPreferences::class)
    fun provideEncryptedSharedPreferences(context: Context): EncryptedSharedPreferences {
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
    fun provideUserLocationDataStore(context: Context): DataStore<UserLocationPoint> =
        DataStoreFactory.create(
            UserLocationPointSerializer(),
            produceFile = { context.dataStoreFile(USER_LOCATION_DATA_STORE_FILE) }
        )

    @Provides
    @Singleton
    fun provideUserDataStore(context: Context): DataStore<UserCredentials> =
        DataStoreFactory.create(
            UserDataSerializer(),
            produceFile = { context.dataStoreFile(USER_DATA_STORE_FILE) }
        )

    @Provides
    @Singleton
    fun provideUserInfoDataStore(context: Context): DataStore<UserInfo> =
        DataStoreFactory.create(
            UserInfoSerializer(),
            produceFile = { context.dataStoreFile(USER_INFO_STORE_FILE) }
        )

    @Provides
    @Singleton
    @IntoMap
    @DependencyKey(DataStorage::class)
    fun provideDataStorage(
        userLocationDataStore: DataStore<UserLocationPoint>,
        userCredentialsDataStore: DataStore<UserCredentials>,
        userInfoDataStore: DataStore<UserInfo>
    ): DataStorage =
        DataStorage(
            userLocationDataStore,
            userCredentialsDataStore,
            userInfoDataStore
        )

    companion object {
        private const val ENCRYPTED_PREFS = "encrypted_prefs"
        private const val USER_LOCATION_DATA_STORE_FILE = "user_location_ds"
        private const val USER_DATA_STORE_FILE = "user_data_ds"
        private const val USER_INFO_STORE_FILE = "user_info_ds"
    }
}