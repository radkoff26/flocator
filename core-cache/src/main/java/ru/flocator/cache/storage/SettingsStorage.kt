package ru.flocator.cache.storage

import ru.flocator.cache.storage.domain.Language
import ru.flocator.core_map.api.configuration.MapConfiguration

interface SettingsStorage {

    fun getMapConfiguration(): MapConfiguration

    fun setMapConfiguration(mapConfiguration: MapConfiguration)

    fun getLanguage(): Language?

    fun setLanguage(language: Language)
}