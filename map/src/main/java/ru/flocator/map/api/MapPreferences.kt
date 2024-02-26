package ru.flocator.map.api

import android.content.Context
import android.content.SharedPreferences
import ru.flocator.map.api.configuration.MapConfiguration

class MapPreferences(context: Context) {
    companion object {
        private const val PREFERENCES_NAME = "MAP_PREFERENCES"
        private const val CONFIGURATION = "CONFIGURATION"
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getMapConfiguration(): MapConfiguration =
        when (preferences.getString(CONFIGURATION, null)) {
            MapConfiguration.MarksOnly::class.java.simpleName -> MapConfiguration.MarksOnly
            MapConfiguration.UsersOnly::class.java.simpleName -> MapConfiguration.UsersOnly
            else -> MapConfiguration.All
        }

    fun setMapConfiguration(mapConfiguration: MapConfiguration) {
        preferences.edit().apply {
            putString(CONFIGURATION, mapConfiguration::class.java.simpleName)
            apply()
        }
    }
}