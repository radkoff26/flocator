package com.example.flocator.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.models.User

class MainFragmentViewModel : ViewModel() {
    private val _friendsLiveData = MutableLiveData<List<User>>(emptyList())
    val friendsLiveData: LiveData<List<User>> = _friendsLiveData

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
        }
        _friendsLiveData.value = list
    }
}