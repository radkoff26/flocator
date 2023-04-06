package com.example.flocator.main.ui.view_models

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.Constants
import com.example.flocator.main.api.ClientAPI
import com.example.flocator.main.models.CameraStatus
import com.example.flocator.main.models.CameraStatusType
import com.example.flocator.main.models.Mark
import com.example.flocator.main.models.User
import com.example.flocator.main.ui.data.MarkGroup
import com.example.flocator.main.utils.MarksDiffUtils
import com.example.flocator.main.utils.MarksUtils
import com.google.gson.GsonBuilder
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.lang.Double.min
import kotlin.math.max

class MainFragmentViewModel : ViewModel() {
    // Data inside of Live Data is non-nullable
    private val _friendsLiveData = MutableLiveData<Map<Long, User>>(HashMap())
    private val _visibleMarksLiveData = MutableLiveData<List<MarkGroup>>(ArrayList())
    private val _cameraStatusLiveData = MutableLiveData(CameraStatus())
    private val _photoCacheLiveData = MutableLiveData<Map<String, Bitmap>>(HashMap())
    private val _userLocationLiveData = MutableLiveData<Point?>(null)

    private val _marks: MutableMap<Long, Mark> = HashMap()
    val marks: Map<Long, Mark>
        get() = _marks

    private var currentVisibleRegion: VisibleRegion? = null
    private var currentScale: Float? = null // TODO: eliminate

    private val friendsHandler: Handler = Handler(Looper.getMainLooper())
    private val marksHandler: Handler = Handler(Looper.getMainLooper())
    private val compositeDisposable = CompositeDisposable()
    private val clientAPI: ClientAPI by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        retrofit.create()
    }

    val friendsLiveData: LiveData<Map<Long, User>> = _friendsLiveData
    val visibleMarksLiveData: LiveData<List<MarkGroup>> = _visibleMarksLiveData
    val cameraStatusLiveData: LiveData<CameraStatus> = _cameraStatusLiveData
    val photoCacheLiveData: LiveData<Map<String, Bitmap>> = _photoCacheLiveData
    val userLocationLiveData: LiveData<Point?> = _userLocationLiveData

    fun startPolling() {
        friendsHandler.post(this::fetchFriends)
        marksHandler.post(this::fetchMarks)
    }

    fun stopPolling() {
        friendsHandler.removeCallbacks(this::fetchFriends)
        marksHandler.removeCallbacks(this::fetchMarks)
    }

    fun updateVisibleRegion(visibleRegion: VisibleRegion, scale: Float) {
        currentVisibleRegion = visibleRegion
        currentScale = scale
        val marksInVisibleRegionGrouped = getVisibleRegionMarksGrouped(visibleRegion, scale)
        if (MarksDiffUtils.isChanged(
                _visibleMarksLiveData.value!!.toList(),
                marksInVisibleRegionGrouped
            )
        ) {
            _visibleMarksLiveData.value = marksInVisibleRegionGrouped
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
        if (_userLocationLiveData.value == null) {
            return
        }
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.setFollowOnUserMark(USER_ID, _userLocationLiveData.value!!)
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
        visibleRegion: VisibleRegion,
        scale: Float
    ): List<MarkGroup> {
        val marksInVisibleRegion =
            getMarksInVisibleRegionOnly(visibleRegion, _marks.values.toList())
        return MarksUtils.groupMarks(marksInVisibleRegion, scale)
    }

    private fun getMarksInVisibleRegionOnly(
        visibleRegion: VisibleRegion,
        marks: List<Mark>
    ): List<Mark> {
        return marks.filter { isInVisibleRegion(it.location, visibleRegion) }
    }

    private fun isInVisibleRegion(point: Point, visibleRegion: VisibleRegion): Boolean {
        val minLongitude =
            min(visibleRegion.bottomRight.longitude, visibleRegion.bottomLeft.longitude)
        val maxLongitude =
            max(visibleRegion.bottomRight.longitude, visibleRegion.bottomLeft.longitude)
        val minLatitude = min(visibleRegion.bottomLeft.latitude, visibleRegion.topLeft.latitude)
        val maxLatitude = max(visibleRegion.bottomLeft.latitude, visibleRegion.topLeft.latitude)
        return (point.latitude in minLongitude..maxLongitude) // TODO: Fix this chaos
                &&
                (point.longitude in minLatitude..maxLatitude)
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
                    },
                    {
                        Log.e(TAG, it.message, it)
                    }
                )
        )
//        friendsHandler.postDelayed(this::fetchFriends, 5000)
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
                    },
                    {
                        Log.e(TAG, it.message, it)
                    }
                )
        )
//        friendsHandler.postDelayed(this::fetchMarks, 10000)
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
        if (currentVisibleRegion != null && currentScale != null) {
            updateVisibleRegion(currentVisibleRegion!!, currentScale!!)
        }
    }

    private fun setCameraPoint(point: Point) {
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.point = point
        _cameraStatusLiveData.value = cameraStatus
    }

    companion object {
        const val TAG = "Main Fragment View Model"
        const val USER_ID = 1L
        const val USER_AVATAR_URL =
            "https://sun9-55.userapi.com/impg/2NrJDQ-paBNyKNiDFFU0ItHSxe4PmpWR-V16fA/9ZkY5ZR55gc.jpg?size=720x1280&quality=95&sign=e2343d8bb5f0039a054c4cb063486f26&type=album"
    }
}