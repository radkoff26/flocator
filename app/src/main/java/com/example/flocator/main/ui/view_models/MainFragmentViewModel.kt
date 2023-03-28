package com.example.flocator.main.ui.view_models

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.models.CameraStatus
import com.example.flocator.main.models.CameraStatusType
import com.example.flocator.main.models.Mark
import com.example.flocator.main.models.User
import com.yandex.mapkit.geometry.Point

class MainFragmentViewModel : ViewModel() {
    // Data inside of Live Data is non-nullable
    private val _friendsLiveData = MutableLiveData<Map<Long, User>>(HashMap())
    private val _marksLiveData = MutableLiveData<Map<Long, Mark>>(HashMap())
    private val _cameraStatusLiveData = MutableLiveData(CameraStatus())
    private val _photoCacheLiveData = MutableLiveData<Map<String, Bitmap>>(HashMap())

    val friendsLiveData: LiveData<Map<Long, User>> = _friendsLiveData
    val marksLiveData: LiveData<Map<Long, Mark>> = _marksLiveData
    val cameraStatusLiveData: LiveData<CameraStatus> = _cameraStatusLiveData
    val photoCacheLiveData: LiveData<Map<String, Bitmap>> = _photoCacheLiveData

    // Method is called when some friend emits new location message to the user
    fun updateUsers(users: List<User>) {
        val map: MutableMap<Long, User> = _friendsLiveData.value!!.toMutableMap()
        for (user in users) {
            map[user.id] = user
            if (_cameraStatusLiveData.value!!.cameraStatusType == CameraStatusType.FOLLOW && _cameraStatusLiveData.value!!.markId == user.id) {
                setCameraPoint(user.point)
            }
        }
        _friendsLiveData.value = map
    }

    fun updateMarks(marks: List<Mark>) {
        val map: MutableMap<Long, Mark> = _marksLiveData.value!!.toMutableMap()
        for (mark in marks) {
            map[mark.markId] = mark
        }
        _marksLiveData.value = map
    }

    fun setCameraPoint(point: Point) {
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.point = point
        _cameraStatusLiveData.value = cameraStatus
    }

    fun setCameraFollowOnFriendMark(friendId: Long) {
        if (_friendsLiveData.value!![friendId] == null) {
            return
        }
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.setFollowOnMark(friendId, _friendsLiveData.value!![friendId]!!.point)
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
}