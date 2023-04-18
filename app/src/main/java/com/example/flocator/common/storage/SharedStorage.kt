package com.example.flocator.common.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.flocator.common.config.SharedPreferencesContraction
import com.example.flocator.common.models.UserData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptedSharedPreferences: EncryptedSharedPreferences
) : UserDataStorage {
    private val defaultSharedPreferences = context.getSharedPreferences(
        SharedPreferencesContraction.User.prefs_name,
        Context.MODE_PRIVATE
    )

    override fun getUserId(): Long? {
        val userId = defaultSharedPreferences.getLong(SharedPreferencesContraction.User.USER_ID, 0)
        return if (userId == 0L) null else userId
    }

    override fun getLogin(): String? {
        return defaultSharedPreferences.getString(SharedPreferencesContraction.User.LOGIN, null)
    }

    override fun getPassword(): String? {
        return encryptedSharedPreferences.getString(SharedPreferencesContraction.User.PASSWORD, null)
    }

    override fun hasUserData(): Boolean {
        return getUserId() != null && getLogin() != null && getPassword() != null
    }

    override fun saveUserData(userData: UserData) {
        defaultSharedPreferences.edit().apply {
            putLong(SharedPreferencesContraction.User.USER_ID, userData.userId)
            putString(SharedPreferencesContraction.User.LOGIN, userData.login)
            apply()
        }
        encryptedSharedPreferences.edit().apply {
            putString(SharedPreferencesContraction.User.PASSWORD, userData.password)
            apply()
        }
    }
}
