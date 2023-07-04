package ru.flocator.core_map.api

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Single
import ru.flocator.core_map.api.configuration.MapConfiguration
import ru.flocator.core_map.api.entity.Mark
import ru.flocator.core_map.api.entity.User

typealias LoadPhotoCallback = (uri: String) -> Single<Bitmap>
typealias OnFriendViewClickCallback = (id: Long) -> Unit
typealias OnMarkViewClickCallback = (id: Long) -> Unit
typealias OnMarkGroupViewClickCallback = (markIds: List<Long>) -> Unit

interface FLocatorMap {
    fun isMapCreated(): Boolean
    fun initialize(
        loadPhotoCallback: LoadPhotoCallback? = null,
        onFriendViewClickCallback: OnFriendViewClickCallback? = null,
        onMarkViewClickCallback: OnMarkViewClickCallback? = null,
        onMarkGroupViewClickCallback: OnMarkGroupViewClickCallback? = null
    )
    fun submitUser(user: User)
    fun submitFriends(friends: List<User>)
    fun submitMarks(marks: List<Mark>)
    fun updateUserLocation(location: LatLng)
    fun moveCameraTo(latLng: LatLng)
    fun followUser(userId: Long)
    fun changeConfiguration(mapConfiguration: MapConfiguration)
}