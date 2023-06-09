package ru.flocator.app.map.utils

import ru.flocator.app.common.storage.db.entities.MarkWithPhotos
import ru.flocator.app.common.storage.db.entities.User
import ru.flocator.app.map.domain.configuration.MapConfiguration
import ru.flocator.app.map.domain.configuration.MapFilterType

object MapFilterUtils {
    fun filterMarksByMapConfiguration(
        marks: List<MarkWithPhotos>,
        mapConfiguration: MapConfiguration
    ): List<MarkWithPhotos> =
        marks.filter {
            doesMarkMatchConfiguration(it, mapConfiguration)
        }

    private fun doesMarkMatchConfiguration(
        mark: MarkWithPhotos,
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
                    if (it.userId != mark.mark.authorId) {
                        return@any false
                    }
                    // TODO: implement more visibility constraints
                    when (it.type) {
                        MapFilterType.PUBLIC -> {
                            return@any mark.mark.isPublic
                        }
                        else -> {
                            return@any true
                        }
                    }
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
                    if (it.userId != user.id) {
                        return@any false
                    }
                    it.type != MapFilterType.PUBLIC
                }
            }
        }
}