package com.example.flocator.main.ui.main

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.common.photo.PhotoCacheLiveData
import com.example.flocator.common.storage.db.ApplicationDatabase
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.main.api.ClientAPI
import com.example.flocator.main.models.*
import com.example.flocator.main.models.dto.MarkDto
import com.example.flocator.main.models.dto.UserLocationDto
import com.example.flocator.main.ui.main.data.UserInfo
import com.example.flocator.main.ui.main.data.MarkGroup
import com.example.flocator.main.utils.MarksDiffUtils
import com.example.flocator.main.utils.MarksUtils
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
@Suppress("UNCHECKED_CAST")
class MainFragmentViewModel @Inject constructor(
    private val clientAPI: ClientAPI,
    applicationDatabase: ApplicationDatabase,
    @ApplicationContext context: Context
) : ViewModel() {
    val Context.dataStore by preferencesDataStore("")
    // Data inside of Live Data is non-nullable
    private val _friendsLiveData = MutableLiveData<Map<Long, User>>(HashMap())
    private val _visibleMarksLiveData = MutableLiveData<List<MarkGroup>>(ArrayList())
    private val _cameraStatusLiveData = MutableLiveData(CameraStatus())
    private val _userLocationLiveData = MutableLiveData<Point?>(null)
    private var _userInfo: UserInfo? = null

    private val _marks: MutableMap<Long, MarkWithPhotos> = HashMap()
    val marks: Map<Long, MarkWithPhotos>
        get() = _marks

    private var currentVisibleRegion: VisibleRegion? = null
    private var mapWidth: Float? = null
    private var markWidth: Float? = null

    private val friendsHandler: Handler = Handler(Looper.getMainLooper())
    private val marksHandler: Handler = Handler(Looper.getMainLooper())
    private val compositeDisposable = CompositeDisposable()

    private val markDao = applicationDatabase.markDao()
    private val markPhotoDao = applicationDatabase.markPhotoDao()
    private val friendsDao = applicationDatabase.userDao()

    val friendsLiveData: LiveData<Map<Long, User>> = _friendsLiveData
    val visibleMarksLiveData: LiveData<List<MarkGroup>> = _visibleMarksLiveData
    val cameraStatusLiveData: LiveData<CameraStatus> = _cameraStatusLiveData
    val userLocationLiveData: LiveData<Point?> = _userLocationLiveData
    val photoCacheLiveData: PhotoCacheLiveData = PhotoCacheLiveData(COMPRESSION_FACTOR)
    val userInfo: UserInfo?
        get() = _userInfo

    private fun initialFetch() {
        val userId = userInfo!!.userId
        compositeDisposable.addAll(
            Single.merge(
                markDao.getAllMarks(),
                clientAPI.getUserAndFriendsMarks(userId)
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.isEmpty()) {
                            updateMarks(emptyList())
                            return@subscribe
                        }
                        if (it[0] is MarkDto) {
                            it as List<MarkDto>
                            updateMarks(
                                it.map(MarkDto::toMarkWithPhotos)
                            )
                        } else {
                            it as List<MarkWithPhotos>
                            updateMarks(it)
                        }
                    },
                    {
                        Log.e(TAG, "Initialization: marks loading failed!", it)
                    }
                ),
            Single.merge(
                friendsDao.getAllFriends(),
                clientAPI.getUserFriendsLocated(userId)
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                    },
                    {
                        Log.e(TAG, "Initialization: friends loading failed!", it)
                    }
                )
        )
    }

    fun requestUserData(userId: Long) {
        compositeDisposable.add(
            clientAPI.getUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userInfo = it
                        initialFetch()
                    },
                    {
                        Log.e(TAG, "requestUserData: ${it.stackTraceToString()}", it)
                    }
                )
        )
    }

    fun postLocation() {
        if (_userInfo == null || _userLocationLiveData.value == null) {
            return
        }
        compositeDisposable.add(
            clientAPI.updateLocation(
                UserLocationDto(
                    _userInfo!!.userId,
                    _userLocationLiveData.value!!
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {

                    },
                    {
                        Log.e(TAG, "postLocation: error", it)
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

    override fun onCleared() {
        compositeDisposable.addAll(
            markDao.updateTable(_marks.values.map(MarkWithPhotos::mark))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Log.d(TAG, "onCleared: saved marks cache")
                    },
                    {
                        Log.e(TAG, "onCleared: error while saving marks cache", it)
                    }
                ),
            markPhotoDao.insertPhotos(
                _marks.values.map(MarkWithPhotos::photos)
                    .fold(ArrayList()) { acc, markPhotos ->
                        acc.addAll(markPhotos)
                        acc
                    }
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Log.d(TAG, "onCleared: saved marks photos cache")
                    },
                    {
                        Log.e(TAG, "onCleared: error while saving marks photos cache", it)
                    }
                ),
            friendsDao.updateTable(_friendsLiveData.value!!.values.toList())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Log.d(TAG, "onCleared: saved friends cache")
                    },
                    {
                        Log.e(TAG, "onCleared: error while friends cache", it)
                    }
                )
        )
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
        marks: List<MarkWithPhotos>
    ): List<MarkWithPhotos> {
        return marks.filter { isInVisibleRegion(it.mark.location, visibleRegion) }
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
        if (userInfo == null) {
            friendsHandler.postDelayed(this::fetchFriends, 5000)
            return
        }
        compositeDisposable.add(
            clientAPI.getUserFriendsLocated(userInfo!!.userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                        friendsHandler.postDelayed(this::fetchFriends, 5000)
                    },
                    {
                        Log.e(TAG, "Failed while loading friends!", it)
                    }
                )
        )
    }

    private fun fetchMarks() {
        if (userInfo == null) {
            friendsHandler.postDelayed(this::fetchMarks, 10000)
            return
        }
        compositeDisposable.add(
            clientAPI.getUserAndFriendsMarks(userInfo!!.userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateMarks(
                            it.map(MarkDto::toMarkWithPhotos)
                        )
                        marksHandler.postDelayed(this::fetchMarks, 10000)
                    },
                    {
                        Log.e(TAG, "Failed while loading marks!", it)
                    }
                )
        )
    }

    private fun updateFriends(users: List<User>) {
        val map: MutableMap<Long, User> = _friendsLiveData.value!!.toMutableMap()
        for (user in users) {
            map[user.id] = user
            if (_cameraStatusLiveData.value!!.cameraStatusType == CameraStatusType.FOLLOW_FRIEND && _cameraStatusLiveData.value!!.markId == user.id) {
                setCameraPoint(user.location)
            }
        }
        _friendsLiveData.value = map
    }

    private fun updateMarks(value: List<MarkWithPhotos>) {
        for (mark in value) {
            _marks[mark.mark.markId] = mark
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
        const val COMPRESSION_FACTOR = 20
    }
}