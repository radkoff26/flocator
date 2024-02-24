package ru.flocator.map.internal.utils

import ru.flocator.data.database.entities.User
import ru.flocator.map.api.configuration.MapConfiguration
import ru.flocator.map.api.configuration.MapFilterType
import ru.flocator.map.api.entity.MapMark

internal object MapFilterUtils {
    fun filterMarksByMapConfiguration(
        marks: List<MapMark>,
        mapConfiguration: MapConfiguration
    ): List<MapMark> =
        marks.filter {
            doesMarkMatchConfiguration(it, mapConfiguration)
        }

    private fun doesMarkMatchConfiguration(
        mark: MapMark,
        configuration: MapConfiguration
    ): Boolean =
        when (configuration) {
            is MapConfiguration.All, MapConfiguration.MarksOnly -> {
                true
            }
            is MapConfiguration.UsersOnly -> {
                false
            }
            is MapConfiguration.SpecialFilter -> {
                configuration.mapFilters.any {
                    if (it.userId != mark.authorId) {
                        return@any false
                    }
                    return@any true
//                    // TODO: implement more visibility constraints
//                    when (it.type) {
//                        MapFilterType.PUBLIC -> {
//                            return@any mark.isPublic
//                        }
//                        else -> {
//                            return@any true
//                        }
//                    }
                }
            }
        }

    fun filterUsersByMapConfiguration(
        users: List<User>,
        mapConfiguration: MapConfiguration
    ): List<User> =
        users.filter {
            doesUserMatchConfiguration(it, mapConfiguration)
        }

    private fun doesUserMatchConfiguration(
        user: User,
        configuration: MapConfiguration
    ): Boolean =
        when (configuration) {
            is MapConfiguration.All, MapConfiguration.UsersOnly -> {
                true
            }
            is MapConfiguration.MarksOnly -> {
                false
            }
            is MapConfiguration.SpecialFilter -> {
                configuration.mapFilters.any {
                    if (it.userId != user.userId) {
                        return@any false
                    }
                    it.type != MapFilterType.PUBLIC
                }
            }
        }
}