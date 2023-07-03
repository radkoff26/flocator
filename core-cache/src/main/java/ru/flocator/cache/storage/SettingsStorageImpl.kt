package ru.flocator.cache.storage

import android.content.Context
import android.content.SharedPreferences
import ru.flocator.core_map.api.configuration.MapConfiguration

class SettingsStorageImpl(context: Context): SettingsStorage {
    companion object {
        private const val PREFERENCES_NAME = "SETTINGS_PREFERENCES"
        private const val CONFIGURATION = "CONFIGURATION"
    }

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getMapConfiguration(): MapConfiguration =
        when (preferences.getString(CONFIGURATION, null)) {
            MapConfiguration.MarksOnly::class.java.simpleName -> MapConfiguration.MarksOnly
            MapConfiguration.UsersOnly::class.java.simpleName -> MapConfiguration.UsersOnly
            else -> MapConfiguration.All
        }

    override fun setMapConfiguration(mapConfiguration: MapConfiguration) {
        preferences.edit().apply {
            putString(CONFIGURATION, mapConfiguration::class.java.simpleName)
            apply()
        }
    }
}