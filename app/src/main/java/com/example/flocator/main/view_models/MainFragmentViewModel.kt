package com.example.flocator.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.User
import com.yandex.mapkit.geometry.Point

class MainFragmentViewModel : ViewModel() {
    private val _friendsLiveData = MutableLiveData<List<User>>(emptyList())
    private val _userCoordinatesLiveData = MutableLiveData<Point>()
    val friendsLiveData: LiveData<List<User>> = _friendsLiveData
    val userCoordinatesLiveData: LiveData<Point> = _userCoordinatesLiveData

    // Method is called when some friend emits new location message to the user
    fun updateUser(user: User) {
        if (_friendsLiveData.value == null) {
            _friendsLiveData.value = emptyList()
        }
        val list: MutableList<User> = _friendsLiveData.value!!.toMutableList()
        val indexOfUser = list.indexOfFirst { it.id == user.id }
        if (list.isEmpty() || indexOfUser == -1) {
            list.add(user)
        } else {
            list[indexOfUser] = user
        }
        _friendsLiveData.value = list
    }

    fun updateUserCoordinates(point: Point) {
        _userCoordinatesLiveData.value = point
    }
}