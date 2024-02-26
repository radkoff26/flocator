package ru.flocator.map.internal.utils

import com.google.android.gms.maps.model.VisibleRegion
import ru.flocator.data.database.entities.User
import ru.flocator.data.models.location.Coordinates
import ru.flocator.map.api.entity.MapMark

internal object VisibilityUtils {
    fun emphasizeVisibleMarks(
        marks: List<MapMark>,
        visibleRegion: VisibleRegion
    ): List<MapMark> {
        return marks.filter {
            isInVisibleRegion(visibleRegion, it.location)
        }
    }

    fun emphasizeVisibleUsers(
        friends: List<User>,
        visibleRegion: VisibleRegion
    ): List<User> {
        return friends.filter {
            isInVisibleRegion(visibleRegion, it.location)
        }
    }

    private fun isInVisibleRegion(visibleRegion: VisibleRegion, coordinates: Coordinates): Boolean {
        val longitudes = listOf(
            visibleRegion.nearLeft.longitude,
            visibleRegion.nearRight.longitude,
            visibleRegion.farLeft.longitude,
            visibleRegion.farRight.longitude
        )
        val latitudes = listOf(
            visibleRegion.nearLeft.latitude,
            visibleRegion.nearRight.latitude,
            visibleRegion.farLeft.latitude,
            visibleRegion.farRight.latitude
        )
        val minLongitude = longitudes.min()
        val maxLongitude = longitudes.max()
        val minLatitude = latitudes.min()
        val maxLatitude = latitudes.max()
        return (coordinates.longitude in minLongitude..maxLongitude)
                &&
                (coordinates.latitude in minLatitude..maxLatitude)
    }
}