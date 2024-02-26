package ru.flocator.feature_settings.api.dependencies

import retrofit2.Retrofit
import ru.flocator.core.cache.global.PhotoLoader
import ru.flocator.core.dependencies.Dependencies
import ru.flocator.data.data_store.info.UserInfoMediator
import ru.flocator.data.data_store.point.UserLocationDataStoreManager
import ru.flocator.data.database.ApplicationDatabase
import ru.flocator.data.database.dao.UserDao
import ru.flocator.data.preferences.LanguagePreferences
import ru.flocator.data.token.TokenPreferences

interface SettingsDependencies : Dependencies {
    val userLocationDataStoreManager: UserLocationDataStoreManager
    val photoLoader: PhotoLoader
    val retrofit: Retrofit
    val languagePreferences: LanguagePreferences
    val tokenPreferences: TokenPreferences
    val userInfoMediator: UserInfoMediator
    val applicationDatabase: ApplicationDatabase
    val userDao: UserDao
}