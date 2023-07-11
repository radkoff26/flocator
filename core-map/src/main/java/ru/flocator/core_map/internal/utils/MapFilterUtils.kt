package ru.flocator.core_map.internal.utils

import ru.flocator.core_map.api.configuration.MapConfiguration
import ru.flocator.core_map.api.configuration.MapFilterType
import ru.flocator.core_map.api.entity.Mark
import ru.flocator.core_map.api.entity.User

internal object MapFilterUtils {
    fun filterMarksByMapConfiguration(
        marks: List<Mark>,
        mapConfiguration: MapConfiguration
    ): List<Mark> =
        marks.filter {
            doesMarkMatchConfiguration(it, mapConfiguration)
        }

    private fun doesMarkMatchConfiguration(
        mark: Mark,
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