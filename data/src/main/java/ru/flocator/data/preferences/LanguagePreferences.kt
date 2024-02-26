package ru.flocator.data.preferences

import android.content.Context
import android.content.SharedPreferences
import ru.flocator.data.models.language.Language

class LanguagePreferences(context: Context) {
    companion object {
        private const val PREFERENCES_NAME = "LANGUAGE_PREFERENCES"
        private const val LANGUAGE = "LANGUAGE"
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getLanguage(): Language? {
        val langString = preferences.getString(LANGUAGE, null) ?: return null
        return Language.valueOf(langString)
    }

    fun setLanguage(language: Language) {
        preferences.edit().apply {
            putString(LANGUAGE, language.toString())
            apply()
        }
    }
}