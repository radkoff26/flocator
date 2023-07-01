package ru.flocator.core_map.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User
import ru.flocator.core_map.domain.camera.CameraStatus
import ru.flocator.core_map.domain.configuration.MapConfiguration
import ru.flocator.core_map.domain.dto.MarkGroup
import ru.flocator.core_map.utils.MapComparingUtils
import ru.flocator.core_map.utils.MapFilterUtils
import ru.flocator.core_map.utils.MarksGroupingUtils
import ru.flocator.core_map.utils.VisibilityUtils

internal class FLocatorMapFragmentViewModel : ViewModel() {
    private val _cameraStatusLiveData: MutableLiveData<CameraStatus> =
        MutableLiveData(CameraStatus())
    val cameraStatusLiveData: LiveData<CameraStatus>
        get() = _cameraStatusLiveData

    private val _visibleMarksLiveData: MutableLiveData<List<MarkGroup>> =
        MutableLiveData(emptyList())
    val visibleMarksLiveData: LiveData<List<MarkGroup>>
        get() = _visibleMarksLiveData

    private val _visibleUsersLiveData: MutableLiveData<List<User>> =
        MutableLiveData(emptyList())
    val visibleUsersLiveData: LiveData<List<User>>
        get() = _visibleUsersLiveData

    private val _userInfoLiveData: MutableLiveData<UserInfo?> = MutableLiveData(null)
    val userInfoLiveData: LiveData<UserInfo?>
        get() = _userInfoLiveData

    private val _userLocationLiveData: MutableLiveData<LatLng?> = MutableLiveData(null)
    val userLocationLiveData: LiveData<LatLng?>
        get() = _userLocationLiveData

    private var _allFriends: Map<Long, User> = emptyMap()
    val allFriends: Map<Long, User>
        get() = _allFriends

    private var allMarks: Map<Long, MarkWithPhotos> = emptyMap()
    private var mapConfiguration: MapConfiguration = MapConfiguration.All
    private var visibleRegion: VisibleRegion? = null
    private var mapWidth: Float? = null
    private var markWidth: Float? = null

    fun changeMapConfigurationTo(mapConfiguration: MapConfiguration) {
        this.mapConfiguration = mapConfiguration
        updateVisibleMarks()
        updateVisibleUsers()
    }

    fun fixCamera() {
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.setFixed()
        _cameraStatusLiveData.value = cameraStatus
    }

    fun makeCameraFollowUser(userId: Long, latLng: LatLng) {
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.setFollowUser(userId, latLng)
        _cameraStatusLiveData.value = cameraStatus
    }

    fun updateFollowingCameraLocation(latLng: LatLng) {
        if (_cameraStatusLiveData.value!!.isCameraFixed) {
            return
        }
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.latLng = latLng
        _cameraStatusLiveData.value = cameraStatus
    }

    fun setMarks(marksList: List<MarkWithPhotos>) {
        allMarks = buildMap {
            marksList.forEach {
                put(it.mark.markId, it)
            }
        }
    }

    fun setFriends(friendsList: List<User>) {
        _allFriends = buildMap {
            friendsList.forEach {
                put(it.id, it)
            }
        }
    }

    fun setUserInfo(userInfo: UserInfo) {
        if (_userInfoLiveData.value != userInfo) {
            _userInfoLiveData.value = userInfo
        }
    }

    fun setUserLocation(latLng: LatLng) {
        if (_userLocationLiveData.value != latLng) {
            _userLocationLiveData.value = latLng
        }
    }

    fun setWidths(mapWidth: Float, markWidth: Float) {
        this.mapWidth = mapWidth
        this.markWidth = markWidth
    }

    fun updateVisibleRegion(visibleRegion: VisibleRegion) {
        if (this.visibleRegion != visibleRegion) {
            this.visibleRegion = visibleRegion
            rearrangeVisibleObjects()
        }
    }

    private fun rearrangeVisibleObjects() {
        updateVisibleMarks()
        updateVisibleUsers()
    }

    private fun updateVisibleMarks() {
        CoroutineScope(Dispatchers.Default).launch {
            val currentVisibleRegion = visibleRegion ?: return@launch
            val currentMapWidth = mapWidth ?: return@launch
            val currentMarkWidth = markWidth ?: return@launch
            val flattenMarks = allMarks.map(Map.Entry<Long, MarkWithPhotos>::value)
            val marks = MapFilterUtils.filterMarksByMapConfiguration(flattenMarks, mapConfiguration)
            val visibleMarks = VisibilityUtils.emphasizeVisibleMarks(marks, currentVisibleRegion)
            val groupedVisibleMarks = MarksGroupingUtils.groupMarks(
                visibleMarks,
                currentVisibleRegion,
                currentMapWidth,
                currentMarkWidth
            )
            val sortedGroupedVisibleMarks =
                groupedVisibleMarks.sortedWith(MapComparingUtils.MarkGroupComparator)
            val currentVisibleMarks = visibleMarksLiveData.value!!
            if (sortedGroupedVisibleMarks != currentVisibleMarks) {
                _visibleMarksLiveData.postValue(sortedGroupedVisibleMarks)
            }
        }
    }

    private fun updateVisibleUsers() {
        CoroutineScope(Dispatchers.Default).launch {
            val currentVisibleRegion = visibleRegion ?: return@launch
            val flattenUsers = _allFriends.map(Map.Entry<Long, User>::value)
            val users = MapFilterUtils.filterUsersByMapConfiguration(flattenUsers, mapConfiguration)
            val visibleUsers =
                VisibilityUtils.emphasizeVisibleUsers(users, currentVisibleRegion)
            val sortedVisibleUsers = visibleUsers.sortedWith(MapComparingUtils.UserComparator)
            val currentVisibleUsers = visibleUsersLiveData.value!!
            if (sortedVisibleUsers != currentVisibleUsers) {
                _visibleUsersLiveData.postValue(visibleUsers)
            }
        }
    }
}