package com.example.flocator.main.ui.view_models

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.api.ClientAPI
import com.example.flocator.main.models.CameraStatus
import com.example.flocator.main.models.CameraStatusType
import com.example.flocator.main.models.Mark
import com.example.flocator.main.models.User
import com.example.flocator.main.ui.data.MarkGroup
import com.example.flocator.main.ui.data.UserInfo
import com.example.flocator.main.utils.MarksDiffUtils
import com.example.flocator.main.utils.MarksUtils
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainFragmentViewModel @Inject constructor(private val clientAPI: ClientAPI) : ViewModel() {
    // Data inside of Live Data is non-nullable
    private val _friendsLiveData = MutableLiveData<Map<Long, User>>(HashMap())
    private val _visibleMarksLiveData = MutableLiveData<List<MarkGroup>>(ArrayList())
    private val _cameraStatusLiveData = MutableLiveData(CameraStatus())
    private val _photoCacheLiveData = MutableLiveData<Map<String, Bitmap>>(HashMap())
    private val _userLocationLiveData = MutableLiveData<Point?>(null)
    private var _userInfo: UserInfo? = null

    private val _marks: MutableMap<Long, Mark> = HashMap()
    val marks: Map<Long, Mark>
        get() = _marks

    private var currentVisibleRegion: VisibleRegion? = null
    private var mapWidth: Float? = null
    private var markWidth: Float? = null

    private val friendsHandler: Handler = Handler(Looper.getMainLooper())
    private val marksHandler: Handler = Handler(Looper.getMainLooper())
    private val compositeDisposable = CompositeDisposable()

    val friendsLiveData: LiveData<Map<Long, User>> = _friendsLiveData
    val visibleMarksLiveData: LiveData<List<MarkGroup>> = _visibleMarksLiveData
    val cameraStatusLiveData: LiveData<CameraStatus> = _cameraStatusLiveData
    val photoCacheLiveData: LiveData<Map<String, Bitmap>> = _photoCacheLiveData
    val userLocationLiveData: LiveData<Point?> = _userLocationLiveData
    val userInfo: UserInfo?
        get() = _userInfo

    fun requestUserData(userId: Long) {
        compositeDisposable.add(
            clientAPI.getUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userInfo = it
                    },
                    {
                        Log.e(TAG, "requestUserData: ${it.stackTraceToString()}", it)
                    }
                )
        )
    }

    fun startPolling() {
        friendsHandler.post(this::fetchFriends)
        marksHandler.post(this::fetchMarks)
    }

    fun stopPolling() {
        friendsHandler.removeCallbacks(this::fetchFriends)
        marksHandler.removeCallbacks(this::fetchMarks)
    }

    fun setWidths(mapWidth: Float, markWidth: Float) {
        this.mapWidth = mapWidth
        this.markWidth = markWidth
    }

    fun updateVisibleRegion(visibleRegion: VisibleRegion) {
        currentVisibleRegion = visibleRegion
        if (mapWidth != null && markWidth != null) {
            val marksInVisibleRegionGrouped = getVisibleRegionMarksGrouped(visibleRegion)
            if (MarksDiffUtils.isChanged(
                    _visibleMarksLiveData.value!!.toList(),
                    marksInVisibleRegionGrouped
                )
            ) {
                _visibleMarksLiveData.value = marksInVisibleRegionGrouped
            }
        }
    }

    fun updateUserLocation(point: Point) {
        if (_cameraStatusLiveData.value!!.cameraStatusType == CameraStatusType.FOLLOW_USER) {
            setCameraPoint(point)
        }
        _userLocationLiveData.value = point
    }

    fun setCameraFollowOnFriendMark(friendId: Long) {
        if (_friendsLiveData.value!![friendId] == null) {
            return
        }
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.setFollowOnFriendMark(friendId, _friendsLiveData.value!![friendId]!!.location)
        _cameraStatusLiveData.value = cameraStatus
    }

    fun setCameraFollowOnUserMark() {
        if (_userLocationLiveData.value == null || userInfo == null) {
            return
        }
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.setFollowOnUserMark(userInfo!!.userId, _userLocationLiveData.value!!)
        _cameraStatusLiveData.value = cameraStatus
    }

    fun setCameraFixed() {
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.setFixed()
        _cameraStatusLiveData.value = cameraStatus
    }

    fun setLoadedPhotoAsync(url: String, bitmap: Bitmap) {
        val map = _photoCacheLiveData.value!!.toMutableMap()
        map[url] = bitmap
        _photoCacheLiveData.postValue(map)
    }

    fun photoCacheContains(url: String): Boolean = _photoCacheLiveData.value!!.containsKey(url)

    fun getCachedPhoto(uri: String) = _photoCacheLiveData.value!![uri]

    override fun onCleared() {
        super.onCleared()
        stopPolling()
        compositeDisposable.dispose()
    }

    private fun getVisibleRegionMarksGrouped(
        visibleRegion: VisibleRegion
    ): List<MarkGroup> {
        val marksInVisibleRegion =
            getMarksInVisibleRegionOnly(visibleRegion, _marks.values.toList())
        return MarksUtils.groupMarks(marksInVisibleRegion, visibleRegion, mapWidth!!, markWidth!!)
    }

    private fun getMarksInVisibleRegionOnly(
        visibleRegion: VisibleRegion,
        marks: List<Mark>
    ): List<Mark> {
        return marks.filter { isInVisibleRegion(it.location, visibleRegion) }
    }

    private fun isInVisibleRegion(point: Point, visibleRegion: VisibleRegion): Boolean {
        val longitudes = listOf(
            visibleRegion.bottomRight.longitude,
            visibleRegion.bottomLeft.longitude,
            visibleRegion.topRight.longitude,
            visibleRegion.topLeft.longitude
        )
        val latitudes = listOf(
            visibleRegion.bottomRight.latitude,
            visibleRegion.bottomLeft.latitude,
            visibleRegion.topRight.latitude,
            visibleRegion.topLeft.latitude
        )
        val minLongitude = longitudes.min()
        val maxLongitude = longitudes.max()
        val minLatitude = latitudes.min()
        val maxLatitude = latitudes.max()
        return (point.longitude in minLongitude..maxLongitude)
                &&
                (point.latitude in minLatitude..maxLatitude)
    }

    private fun fetchFriends() {
        compositeDisposable.add(
            clientAPI.getUserFriendsLocated(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateUsers(it)
                        Log.d(TAG, "Fetched users from server $it")
                        friendsHandler.postDelayed(this::fetchFriends, 5000)
                    },
                    {
                        Log.e(TAG, it.message, it)
                    }
                )
        )
    }

    private fun fetchMarks() {
        compositeDisposable.add(
            clientAPI.getUserAndFriendsMarks(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateMarks(it)
                        Log.d(TAG, "Fetched marks from server $it")
                        friendsHandler.postDelayed(this::fetchMarks, 10000)
                    },
                    {
                        Log.e(TAG, it.message, it)
                    }
                )
        )
    }

    private fun updateUsers(users: List<User>) {
        val map: MutableMap<Long, User> = _friendsLiveData.value!!.toMutableMap()
        for (user in users) {
            map[user.id] = user
            if (_cameraStatusLiveData.value!!.cameraStatusType == CameraStatusType.FOLLOW_FRIEND && _cameraStatusLiveData.value!!.markId == user.id) {
                setCameraPoint(user.location)
            }
        }
        _friendsLiveData.value = map
    }

    private fun updateMarks(value: List<Mark>) {
        for (mark in value) {
            _marks[mark.markId] = mark
        }
        if (currentVisibleRegion != null && mapWidth != null && markWidth != null) {
            updateVisibleRegion(currentVisibleRegion!!)
        }
    }

    private fun setCameraPoint(point: Point) {
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.point = point
        _cameraStatusLiveData.value = cameraStatus
    }

    companion object {
        const val TAG = "Main Fragment View Model"
    }
}