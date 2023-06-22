package ru.flocator.core_data_store.store

import androidx.datastore.core.DataStore
import ru.flocator.core_data_store.point.UserLocationPoint
import ru.flocator.core_data_store.user.data.UserCredentials
import ru.flocator.core_data_store.user.info.UserInfo

class DataStorage(
    val userLocationDataStore: DataStore<UserLocationPoint>,
    val userCredentialsDataStore: DataStore<UserCredentials>,
    val userInfoDataStore: DataStore<UserInfo>
)