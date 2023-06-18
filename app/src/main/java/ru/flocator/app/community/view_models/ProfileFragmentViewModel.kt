package ru.flocator.app.community.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.flocator.core_api.api.MainRepository
import ru.flocator.core_client.UserApi
import ru.flocator.core_dto.user.FriendRequests
import ru.flocator.core_dto.user.Friends
import ru.flocator.core_dto.user.TargetUser
import ru.flocator.app.community.fragments.ProfileFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

typealias UserNewFriendActionListener = (persons: List<TargetUser>) -> Unit

@HiltViewModel
@Suppress("UNCHECKED_CAST")
class ProfileFragmentViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {
    private val _friendsLiveData = MutableLiveData<MutableList<Friends>?>()
    private val _newFriendsLiveData = MutableLiveData<MutableList<FriendRequests>?>()
    private val _currentUserLiveData = MutableLiveData<TargetUser>()
    var friendsLiveData: MutableLiveData<MutableList<Friends>?> = _friendsLiveData
    var newFriendsLiveData: MutableLiveData<MutableList<FriendRequests>?> = _newFriendsLiveData
    val currentUserLiveData: LiveData<TargetUser> = _currentUserLiveData
    private val userId = repository.userCredentialsCache.getUserCredentials().blockingGet().userId
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var userApi: UserApi

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

    private fun updateUser(user: TargetUser) {
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

    private val listener: UserNewFriendActionListener = {
        _newFriendsLiveData.value = it as MutableList<FriendRequests>
    }

    companion object {
        const val TAG = "ProfileFragment"
    }

}