package ru.flocator.data.token

import android.content.Context
import android.content.SharedPreferences

class TokenPreferences(context: Context) {
    private val tokensPreferences =
        context.getSharedPreferences(TOKEN_PREFERENCES, Context.MODE_PRIVATE)

    @Synchronized
    fun getAccessToken(): String? {
        return tokensPreferences.getString(ACCESS_TOKEN, null)
    }

    @Synchronized
    fun setAccessToken(token: String) {
        tokensPreferences.setString(ACCESS_TOKEN, token)
    }

    @Synchronized
    fun getRefreshToken(): String? {
        return tokensPreferences.getString(REFRESH_TOKEN, null)
    }

    @Synchronized
    fun updateTokens(refreshToken: String, accessToken: String) {
        setRefreshToken(refreshToken)
        setAccessToken(accessToken)
    }

    @Synchronized
    fun setRefreshToken(token: String) {
        tokensPreferences.setString(REFRESH_TOKEN, token)
    }

    @Synchronized
    fun clear() {
        setRefreshToken("")
        setAccessToken("")
    }

    private fun SharedPreferences.setString(key: String, value: String) = edit().apply {
        putString(key, value)
        apply()
    }

    companion object {
        private const val TOKEN_PREFERENCES = "TOKENS"
        private const val ACCESS_TOKEN = "ACCESS"
        private const val REFRESH_TOKEN = "REFRESH"
    }
}