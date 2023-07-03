package ru.flocator.cache.storage

import ru.flocator.core_map.api.configuration.MapConfiguration

interface SettingsStorage {

    fun getMapConfiguration(): MapConfiguration

    fun setMapConfiguration(mapConfiguration: MapConfiguration)
}