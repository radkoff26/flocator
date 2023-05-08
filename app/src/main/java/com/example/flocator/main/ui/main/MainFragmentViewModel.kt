package com.example.flocator.main.ui.main

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.common.cache.runtime.PhotoState
import com.example.flocator.common.connection.watcher.ConnectionLiveData
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.common.storage.storage.point.UserLocationPoint
import com.example.flocator.common.storage.storage.user.info.UserInfo
import com.example.flocator.main.models.*
import com.example.flocator.main.ui.main.data.MarkGroup
import com.example.flocator.main.utils.MarksDiffUtils
import com.example.flocator.main.utils.MarksUtils
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class MainFragmentViewModel @AssistedInject constructor(
    private val repository: MainRepository,
    @Assisted private val connectionLiveData: ConnectionLiveData
) : ViewModel() {
    val maxPhotoCacheSize = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 2

    // Data inside of Live Data is non-nullable
    private val _friendsLiveData = MutableLiveData<Map<Long, User>>(HashMap())
    private val _visibleMarksLiveData = MutableLiveData<List<MarkGroup>>(ArrayList())
    private val _cameraStatusLiveData = MutableLiveData(CameraStatus())
    private val _userLocationLiveData = MutableLiveData<Point?>(null)
    private val _photoCacheLiveData = MutableLiveData<LruCache<String, PhotoState>>(
        object : LruCache<String, PhotoState>(maxPhotoCacheSize) {
            override fun sizeOf(key: String?, value: PhotoState?): Int {
                return if (value is PhotoState.Loaded) value.bitmap.byteCount / 1024 else 0
            }
        }
    )
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

    val friendsLiveData: LiveData<Map<Long, User>> = _friendsLiveData
    val visibleMarksLiveData: LiveData<List<MarkGroup>> = _visibleMarksLiveData
    val cameraStatusLiveData: LiveData<CameraStatus> = _cameraStatusLiveData
    val userLocationLiveData: LiveData<Point?> = _userLocationLiveData
    val photoCacheLiveData: LiveData<LruCache<String, PhotoState>> = _photoCacheLiveData
    val userInfo: UserInfo?
        get() = _userInfo

    private fun initialFetch() {
        val userId = userInfo!!.userId
        compositeDisposable.addAll(
            repository.restApi.getAllFriendsOfUser(userId, connectionLiveData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                    },
                    {
                        Log.e(TAG, "Initialization: marks loading failed!", it)
                    }
                ),
            repository.restApi.getMarksForUser(userId, connectionLiveData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateMarks(it)
                    },
                    {
                        Log.e(TAG, "Initialization: friends loading failed!", it)
                    }
                )
        )
    }

    fun loadPhoto(uri: String) {
        if (_photoCacheLiveData.value!!.get(uri) == null) {
            updateCacheState(uri, PhotoState.Loading)
        }
        compositeDisposable.add(
            repository.photoLoader.getPhoto(uri, COMPRESSION_FACTOR)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateCacheState(uri, PhotoState.Loaded(it))
                    },
                    {
                        updateCacheState(uri, PhotoState.Failed(it))
                    }
                )
        )
    }

    private fun updateCacheState(uri: String, photoState: PhotoState) {
        _photoCacheLiveData.value!!.put(uri, photoState)
        _photoCacheLiveData.value = _photoCacheLiveData.value
    }

    private fun retrieveDataFromCache() {
        compositeDisposable.addAll(
            repository.userInfoCache.getUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userInfo = it
                    },
                    {

                    }
                ),
            repository.locationCache.getUserLocationData()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userLocationLiveData.postValue(
                            Point(
                                it.latitude,
                                it.longitude
                            )
                        )
                    },
                    {
                        Log.e(
                            TAG,
                            "initialFetch: error while fetching user location from cache",
                            it
                        )
                    }
                ),
            repository.cacheDatabase.retrieveFriendsFromCache()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                    },
                    {
                        Log.e(TAG, "retrieveDataFromCache: error while loading friends cache!", it)
                    }
                ),
            repository.cacheDatabase.retrieveMarksFromCache()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateMarks(it)
                    },
                    {
                        Log.e(TAG, "retrieveDataFromCache: error while loading marks cache!", it)
                    }
                ),
        )
    }

    fun requestInitialLoading() {
        retrieveDataFromCache()
        compositeDisposable.add(
            repository.restApi.getCurrentUserInfo(connectionLiveData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userInfo = it
                        repository.userInfoCache.updateUserInfo(it)
                        initialFetch()
                    },
                    {
                        Log.e(
                            TAG,
                            "requestUserData: ${it.stackTraceToString()}",
                            it
                        )
                    }
                )
        )
    }

    fun postLocation() {
        if (_userInfo == null || _userLocationLiveData.value == null) {
            return
        }
        compositeDisposable.add(
            repository.restApi.postUserLocation(
                _userInfo!!.userId,
                _userLocationLiveData.value!!,
                connectionLiveData
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_LOCATION_POST.toLong())
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
        CoroutineScope(Dispatchers.IO).launch {
            repository.locationCache.updateUserLocationData(
                UserLocationPoint(
                    point.latitude,
                    point.longitude
                )
            )
        }
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
            Log.i(TAG, "POST_DELAYED_ON_USER_ID_NULL")
            friendsHandler.postDelayed(this::fetchFriends, 5000)
            return
        }
        compositeDisposable.add(
            repository.restApi.getAllFriendsOfUser(userInfo!!.userId, connectionLiveData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_FRIENDS_FETCHING.toLong())
                .subscribe(
                    {
                        Log.i(TAG, "fetchFriends: fetched!")
                        updateFriends(it)
                        Log.i(TAG, "POST_DELAYED_ON_SUCCESS")
                        friendsHandler.postDelayed(this::fetchFriends, 5000)
                    },
                    {
                        Log.e(TAG, "Failed while loading friends!", it)
                        friendsHandler.postDelayed(this::fetchFriends, 5000)
                    }
                )
        )
    }

    private fun fetchMarks() {
        if (userInfo == null) {
            marksHandler.postDelayed(this::fetchMarks, 10000)
            return
        }
        compositeDisposable.add(
            repository.restApi.getMarksForUser(userInfo!!.userId, connectionLiveData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_MARKS_FETCHING.toLong())
                .subscribe(
                    {
                        updateMarks(it)
                        marksHandler.postDelayed(this::fetchMarks, 10000)
                    },
                    {
                        Log.e(TAG, "Failed while loading marks!", it)
                        marksHandler.postDelayed(this::fetchMarks, 10000)
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

    @AssistedFactory
    interface Factory {
        fun build(connectionLiveData: ConnectionLiveData): MainFragmentViewModel
    }

    companion object {
        const val TAG = "Main Fragment View Model"
        const val COMPRESSION_FACTOR = 20
        const val TIMES_TO_RETRY_LOCATION_POST = 5
        const val TIMES_TO_RETRY_FRIENDS_FETCHING = 10
        const val TIMES_TO_RETRY_MARKS_FETCHING = 7
    }
}