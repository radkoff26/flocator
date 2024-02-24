package ru.flocator.map.internal.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import ru.flocator.data.database.entities.User
import ru.flocator.map.api.configuration.MapConfiguration
import ru.flocator.map.api.entity.MapMark
import ru.flocator.map.internal.domain.camera.CameraStatus
import ru.flocator.map.internal.domain.entity.MarkGroup
import ru.flocator.map.internal.extensions.toCoordinates
import ru.flocator.map.internal.extensions.toLatLng
import ru.flocator.map.internal.utils.MapComparingUtils
import ru.flocator.map.internal.utils.MapFilterUtils
import ru.flocator.map.internal.utils.MarksGroupingUtils
import ru.flocator.map.internal.utils.VisibilityUtils
import kotlin.concurrent.thread

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

    private val _targetUserLiveData: MutableLiveData<User?> = MutableLiveData(null)
    val targetUserLiveData: LiveData<User?>
        get() = _targetUserLiveData

    var allFriends: Map<Long, User> = emptyMap()
        private set

    private var allMarks: Map<Long, MapMark> = emptyMap()
    private var mapConfiguration: MapConfiguration = MapConfiguration.All
    private var visibleRegion: VisibleRegion? = null
    private var mapWidth: Float? = null
    private var markWidth: Float? = null

    fun changeMapConfigurationTo(mapConfiguration: MapConfiguration) {
        if (mapConfiguration != this.mapConfiguration) {
            this.mapConfiguration = mapConfiguration
            rearrangeVisibleObjects()
        }
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

    fun setMarks(marksList: List<MapMark>) {
        allMarks = buildMap {
            marksList.forEach {
                put(it.markId, it)
            }
        }
        updateVisibleMarks()
    }

    fun setFriends(friendsList: List<User>) {
        allFriends = buildMap {
            friendsList.forEach {
                val cameraStatus = _cameraStatusLiveData.value!!
                if (!cameraStatus.isCameraFixed && it.userId == cameraStatus.userId) {
                    updateFollowingCameraLocation(it.location.toLatLng())
                }
                put(it.userId, it)
            }
        }
        updateVisibleUsers()
    }

    fun setUserInfo(user: User) {
        val cameraStatus = _cameraStatusLiveData.value!!
        if (!cameraStatus.isCameraFixed && user.userId == cameraStatus.userId) {
            updateFollowingCameraLocation(user.location.toLatLng())
        }
        _targetUserLiveData.value = user
        // Enforcing marks to be redrawn (updated) since user has changed
        _visibleMarksLiveData.value = _visibleMarksLiveData.value
    }

    fun setUserLocation(location: LatLng) {
        val targetUser = _targetUserLiveData.value
        if (targetUser != null && targetUser.location.toLatLng() != location) {
            val cameraStatus = cameraStatusLiveData.value!!
            if (!cameraStatus.isCameraFixed && cameraStatus.userId == targetUser.userId) {
                updateFollowingCameraLocation(location)
            }
            _targetUserLiveData.value = targetUser.copy(
                location = location.toCoordinates()
            )
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

    fun hasTargetUser(): Boolean = targetUserLiveData.value != null

    fun hasMarks(): Boolean =
        visibleMarksLiveData.value != null || visibleMarksLiveData.value!!.isNotEmpty()

    fun hasFriends(): Boolean =
        visibleUsersLiveData.value != null || visibleUsersLiveData.value!!.isNotEmpty()

    private fun updateFollowingCameraLocation(latLng: LatLng) {
        if (_cameraStatusLiveData.value!!.isCameraFixed) {
            return
        }
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.latLng = latLng
        _cameraStatusLiveData.value = cameraStatus
    }

    private fun rearrangeVisibleObjects() {
        updateVisibleMarks()
        updateVisibleUsers()
    }

    private fun updateVisibleMarks() {
        thread {
            val currentVisibleRegion = visibleRegion ?: return@thread
            val currentMapWidth = mapWidth ?: return@thread
            val currentMarkWidth = markWidth ?: return@thread

            val flattenMarks = allMarks.map(Map.Entry<Long, MapMark>::value)
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
        thread {
            val currentVisibleRegion = visibleRegion ?: return@thread
            val flattenUsers = allFriends.map(Map.Entry<Long, User>::value)
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