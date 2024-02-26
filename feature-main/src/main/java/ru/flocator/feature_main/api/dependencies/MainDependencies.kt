package ru.flocator.feature_main.api.dependencies

import retrofit2.Retrofit
import ru.flocator.core.cache.global.PhotoLoader
import ru.flocator.core.dependencies.Dependencies
import ru.flocator.data.data_store.info.UserInfoMediator
import ru.flocator.data.data_store.point.UserLocationDataStoreManager
import ru.flocator.data.database.ApplicationDatabase
import ru.flocator.data.database.dao.MarkDao
import ru.flocator.data.database.dao.UserDao
import ru.flocator.data.preferences.LanguagePreferences
import ru.flocator.map.api.MapPreferences

interface MainDependencies : Dependencies {
    val userLocationDataStoreManager: UserLocationDataStoreManager
    val retrofit: Retrofit
    val languagePreferences: LanguagePreferences
    val mapPreferences: MapPreferences
    val photoLoader: PhotoLoader
    val applicationDatabase: ApplicationDatabase
    val userDao: UserDao
    val markDao: MarkDao
    val userInfoMediator: UserInfoMediator
}
