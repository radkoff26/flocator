package ru.flocator.map.api

import android.graphics.Bitmap
import io.reactivex.Single
import ru.flocator.data.database.entities.User
import ru.flocator.data.models.location.Coordinates
import ru.flocator.map.api.configuration.MapConfiguration
import ru.flocator.map.api.entity.MapMark

typealias LoadPhotoCallback = (uri: String) -> Single<Bitmap>
typealias OnFriendViewClickCallback = (id: Long) -> Unit
typealias OnMarkViewClickCallback = (id: Long) -> Unit
typealias OnMarkGroupViewClickCallback = (markIds: List<Long>) -> Unit

interface FLocatorMap {
    fun initialize(
        mapConfiguration: MapConfiguration = MapConfiguration.All,
        loadPhotoCallback: LoadPhotoCallback? = null,
        onFriendViewClickCallback: OnFriendViewClickCallback? = null,
        onMarkViewClickCallback: OnMarkViewClickCallback? = null,
        onMarkGroupViewClickCallback: OnMarkGroupViewClickCallback? = null
    )

    fun submitUser(user: User)
    fun submitFriends(friends: List<User>)
    fun submitMarks(marks: List<MapMark>)
    fun updateUserLocation(location: Coordinates)
    fun moveCameraTo(latLng: Coordinates)
    fun followUser(userId: Long)
    fun changeConfiguration(mapConfiguration: MapConfiguration)
}