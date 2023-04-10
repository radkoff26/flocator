package com.example.flocator.common.config

sealed class SharedPreferencesContraction(val prefs_name: String) {
    object User: SharedPreferencesContraction("USER") {
        const val USER_ID = "USER_ID"
        const val LOGIN = "LOGIN"
        const val PASSWORD = "PASSWORD"
    }
}
