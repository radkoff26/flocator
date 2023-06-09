package ru.flocator.app.main.ui.map.domain.map

import android.graphics.Bitmap
import ru.flocator.app.common.storage.db.entities.MarkWithPhotos
import ru.flocator.app.common.storage.db.entities.User
import ru.flocator.app.common.storage.store.user.info.UserInfo
import ru.flocator.app.main.ui.map.domain.configuration.MapConfiguration
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Single

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