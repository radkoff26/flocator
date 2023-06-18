package ru.flocator.app.map.domain.map

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Single
import ru.flocator.app.map.domain.configuration.MapConfiguration
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User

typealias LoadPhotoCallback = (uri: String) -> Single<Bitmap>
typealias OnFriendViewClickCallback = (id: Long) -> Unit
typealias OnMarkViewClickCallback = (id: Long) -> Unit
typealias OnMarkGroupViewClickCallback = (marks: List<MarkWithPhotos>) -> Unit

interface FLocatorMap {
    fun isMapCreated(): Boolean
    fun initialize(
        loadPhotoCallback: LoadPhotoCallback? = null,
        onFriendViewClickCallback: OnFriendViewClickCallback? = null,
        onMarkViewClickCallback: OnMarkViewClickCallback? = null,
        onMarkGroupViewClickCallback: OnMarkGroupViewClickCallback? = null
    )
    fun submitUser(userInfo: UserInfo)
    fun submitFriends(friends: List<User>)
    fun submitMarks(marks: List<MarkWithPhotos>)
    fun updateUserLocation(location: LatLng)
    fun moveCameraTo(latLng: LatLng)
    fun followUser(userId: Long)
    fun changeConfiguration(mapConfiguration: MapConfiguration)
}