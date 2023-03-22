package com.example.flocator.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.models.CameraStatus
import com.example.flocator.main.models.CameraStatusType
import com.example.flocator.main.models.User
import com.yandex.mapkit.geometry.Point

class MainFragmentViewModel : ViewModel() {
    private val _friendsLiveData = MutableLiveData(emptyList<User>())
    private val _cameraStatusLiveData = MutableLiveData(CameraStatus())

    val friendsLiveData: LiveData<List<User>> = _friendsLiveData
    // CameraStatus is not nullable
    val cameraStatusLiveData: LiveData<CameraStatus> = _cameraStatusLiveData

    // Method is called when some friend emits new location message to the user
    fun updateUsers(users: List<User>) {
        if (_friendsLiveData.value == null) {
            _friendsLiveData.value = emptyList()
        }
        val list: MutableList<User> = _friendsLiveData.value!!.toMutableList()
        for (user in users) {
            val indexOfUser = list.indexOfFirst { it.id == user.id }
            if (list.isEmpty() || indexOfUser == -1) {
                list.add(user)
            } else {
                list[indexOfUser] = user
            }
            if (_cameraStatusLiveData.value!!.cameraStatusType == CameraStatusType.FOLLOW && _cameraStatusLiveData.value!!.markId == user.id) {
                setCameraPoint(user.point)
            }
        }
        _friendsLiveData.value = list
    }

    fun setCameraPoint(point: Point) {
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.point = point
        _cameraStatusLiveData.value = cameraStatus
    }

    fun setCameraFollowOnMark(markId: Long) {
        if (_friendsLiveData.value == null) {
            return
        }
        val indexOfUser = _friendsLiveData.value!!.indexOfFirst { it.id == markId }
        if (indexOfUser == -1) {
            return
        }
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.setFollowOnMark(markId, _friendsLiveData.value!![indexOfUser].point)
        _cameraStatusLiveData.value = cameraStatus
    }

    fun setCameraFixed() {
        val cameraStatus = _cameraStatusLiveData.value!!
        cameraStatus.setFixed()
        _cameraStatusLiveData.value = cameraStatus
    }
}