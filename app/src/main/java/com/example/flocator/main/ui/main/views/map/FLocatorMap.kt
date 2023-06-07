package com.example.flocator.main.ui.main.views.map

import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.common.storage.store.user.info.UserInfo
import com.google.android.gms.maps.model.LatLng

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