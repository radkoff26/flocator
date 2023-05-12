package com.example.flocator.community.view_models

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.Application
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.community.adapters.FriendActionListener
import com.example.flocator.community.api.UserApi
import com.example.flocator.community.data_classes.FriendRequests
import com.example.flocator.community.data_classes.Friends
import com.example.flocator.community.data_classes.User
import com.example.flocator.community.data_classes.UserExternal
import com.example.flocator.community.fragments.ProfileFragment
import com.example.flocator.community.fragments.UserRepository
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Inject

typealias UserNewFriendActionListener = (persons: List<User>) -> Unit

@HiltViewModel
@Suppress("UNCHECKED_CAST")
class ProfileFragmentViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {
    private val _friendsLiveData = MutableLiveData<MutableList<Friends>?>()
    private val _newFriendsLiveData = MutableLiveData<MutableList<FriendRequests>?>()
    private val _currentUserLiveData = MutableLiveData<User>()
    var friendsLiveData: MutableLiveData<MutableList<Friends>?> = _friendsLiveData
    var newFriendsLiveData: MutableLiveData<MutableList<FriendRequests>?> = _newFriendsLiveData
    val currentUserLiveData: LiveData<User> = _currentUserLiveData
    private val userId = repository.userDataCache.getUserData().blockingGet().userId
    private val compositeDisposable = CompositeDisposable()


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
        compositeDisposable.dispose()
    }

    fun getCurrentUserId(): Long {
        return userId
    }

    fun fetchUser() {
        compositeDisposable.add(
            userApi.getUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateUser(it)
                    },
                    {
                        Log.e(ProfileFragment.TAG, it.message, it)
                    })
        )
    }

    private fun updateUser(user: User) {
        _currentUserLiveData.value = user
        _friendsLiveData.value = user.friends
        _newFriendsLiveData.value = user.friendRequests
    }

    fun cancelPerson(user: FriendRequests): Int {
        compositeDisposable.add(
            repository.restApi.rejectNewFriend(userId, user.userId!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(TAG, "rejectFriend ERROR", it)
                    }
                )
        )
        val index = _newFriendsLiveData.value?.indexOfFirst { it.userId == user.userId?.toLong() }
        val newFriends: MutableList<FriendRequests>? = _newFriendsLiveData.value
        if (index == -1) {
            return -1
        }
        if (index != null) {
            newFriends?.removeAt(index)
        }
        _newFriendsLiveData.value = newFriends
        return newFriends?.size ?: 0
    }

    fun acceptPerson(user: FriendRequests): Int {
        compositeDisposable.add(
            repository.restApi.acceptNewFriend(userId, user.userId!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(TAG, "acceptFriend ERROR", it)
                    }
                )
        )

        val findingPerson: Friends = Friends(-1, "", "", "")
        val index = _newFriendsLiveData.value?.indexOfFirst { it.userId == user.userId }
        val newFriends: MutableList<FriendRequests>? = _newFriendsLiveData.value
        val friends: MutableList<Friends>? = _friendsLiveData.value
        if (index != null && index != -1) {
            findingPerson.userId = _newFriendsLiveData.value?.get(index)!!.userId
            findingPerson.firstName = _newFriendsLiveData.value?.get(index)!!.firstName
            findingPerson.lastName = _newFriendsLiveData.value?.get(index)!!.lastName
            findingPerson.avatarUri = _newFriendsLiveData.value?.get(index)!!.avatarUri
            newFriends!!.removeAt(index)
            friends!!.add(findingPerson)
        }
        _newFriendsLiveData.value = newFriends
        _friendsLiveData.value = friends
        return newFriends?.size ?: 0
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
        _newFriendsLiveData.value = it as MutableList<FriendRequests>
    }

    companion object{
        const val TAG = "ProfileFragment"
    }

}