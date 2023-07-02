package ru.flocator.core_map.internal.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User

internal object VisibilityUtils {
    fun emphasizeVisibleMarks(
        marks: List<MarkWithPhotos>,
        visibleRegion: VisibleRegion
    ): List<MarkWithPhotos> {
        return marks.filter {
            isInVisibleRegion(visibleRegion, it.mark.location)
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

    private fun isInVisibleRegion(visibleRegion: VisibleRegion, latLng: LatLng): Boolean {
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
        return (latLng.longitude in minLongitude..maxLongitude)
                &&
                (latLng.latitude in minLatitude..maxLatitude)
    }
}