package com.example.flocator.community.view_models

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.Application
import com.example.flocator.community.adapters.FriendActionListener
import com.example.flocator.community.api.UserApi
import com.example.flocator.community.data_classes.User
import com.example.flocator.community.fragments.ProfileFragment
import com.example.flocator.community.fragments.UserRepository
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

typealias UserNewFriendActionListener = (persons: List<User>) -> Unit

class ProfileFragmentViewModel : ViewModel() {
    private val _friendsLiveData = MutableLiveData<MutableList<User>?>()
    private val _newFriendsLiveData = MutableLiveData<MutableList<User>?>()
    private val _currentUserLiveData = MutableLiveData<User>()
    var friendsLiveData: MutableLiveData<MutableList<User>?> = _friendsLiveData
    var newFriendsLiveData: MutableLiveData<MutableList<User>?> = _newFriendsLiveData
    val currentUserLiveData: LiveData<User> = _currentUserLiveData
    private var newFriendsListeners = mutableListOf<List<User>>()
    private var friendsListeners = mutableListOf<List<User>>()
    private var listeners = mutableListOf<UserNewFriendActionListener>()

    private val userApi: UserApi by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://kernelpunik.ru:8080/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        retrofit.create()
    }

    /*fun load() {
        addListener(listener)
    }*/

    override fun onCleared() {
        super.onCleared()
        //removeListener(listener)
    }

    fun fetchFriends() {
        userApi.getUserFriends(1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    updateFriends(it)
                },
                {
                    Log.e(ProfileFragment.TAG, it.message, it)
                })
    }

    private fun updateFriends(users: List<User>) {
        _friendsLiveData.value = users as MutableList<User>
    }

    fun fetchNewFriends() {
        userApi.getUserFriends(1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    updateNewFriends(it)
                },
                {
                    Log.e(ProfileFragment.TAG, it.message, it)
                })

    }

    private fun updateNewFriends(users: List<User>) {
        _newFriendsLiveData.value = users as MutableList<User>
    }

    fun fetchUser() {
        userApi.getUser(1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    updateUser(it)
                },
                {
                    Log.e(ProfileFragment.TAG, it.message, it)
                })
    }

    private fun updateUser(user: User) {
        _currentUserLiveData.value = user
    }

    fun cancelPerson(user: User): Int {
        val index = _newFriendsLiveData.value?.indexOfFirst { it.id == user.id }
        val newFriends: MutableList<User>? = _newFriendsLiveData.value
        if (index == -1) {
            return -1
        }
        if (index != null) {
            newFriends?.removeAt(index)
        }
        _newFriendsLiveData.value = newFriends
        return newFriends?.size ?: 0
        //notifyChanges()
    }

    fun acceptPerson(user: User): Int {
        val findingPerson: User
        val index = _newFriendsLiveData.value?.indexOfFirst { it.id == user.id }
        val newFriends: MutableList<User>? = _newFriendsLiveData.value
        val friends: MutableList<User>? = _friendsLiveData.value
        if (index != null && index != -1) {
            findingPerson = _newFriendsLiveData.value?.get(index) as User
            newFriends!!.removeAt(index)
            friends!!.add(findingPerson)
        }
        _newFriendsLiveData.value = newFriends
        _friendsLiveData.value = friends
        return newFriends?.size ?: 0
        //notifyChanges()
    }

    /*private fun addListener(listener: UserNewFriendActionListener) {
        listeners.add(listener)
        listener.invoke(newFriendsLiveData.value as List<User>)
    }

    private fun removeListener(listener: UserNewFriendActionListener) {
        listeners.remove(listener)
        listener.invoke(newFriendsLiveData.value as List<User>)
    }

    private fun notifyChanges() =
        listeners.forEach { it.invoke(newFriendsLiveData.value as List<User>) }*/

    private val listener: UserNewFriendActionListener = {
        _newFriendsLiveData.value = it as MutableList<User>
    }

}