package ru.flocator.feature_community.internal.ui.view_models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.base.view_model.BaseViewModel
import ru.flocator.core.base.view_model.UiState
import ru.flocator.feature_community.internal.data.model.UserItem
import ru.flocator.feature_community.internal.data.model.UserProfile
import ru.flocator.feature_community.internal.data.repository.FriendshipRepository
import ru.flocator.feature_community.internal.data.repository.UserRepository
import javax.inject.Inject

internal class ProfileViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository,
    private val userRepository: UserRepository,
) : BaseViewModel<ProfileViewModel.UiData>() {
    private val _friendRequestsLiveData = MutableLiveData<List<UserItem>>()
    var friendRequestsLiveData: MutableLiveData<List<UserItem>> = _friendRequestsLiveData

    private val compositeDisposable = CompositeDisposable()

    override fun loadData() {
        loadUserProfileAndFriends()
        loadUserFriendRequests()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun loadUserProfileAndFriends() {
        compositeDisposable.add(
            Observable.zip(
                userRepository.getUserProfileInfo(),
                userRepository.getFriendsOfUser()
            ) { profileInfo, friends ->
                UiData(
                    profileInfo,
                    friends
                )
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        toLoaded(it)
                    },
                    {
                        Log.e(TAG, it.message, it)
                        toFailed(it)
                    }
                )
        )
    }

    private fun loadUserFriendRequests() {
        compositeDisposable.add(
            userRepository.getFriendRequests()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _friendRequestsLiveData.value = it
                    },
                    {
                        Log.e(TAG, it.message, it)
                    }
                )
        )
    }

    fun rejectPerson(user: UserItem): Int {
        compositeDisposable.add(
            friendshipRepository.rejectNewFriend(user.userId)
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

        val index = _friendRequestsLiveData.value?.indexOfFirst { it.userId == user.userId }
        val newFriends: MutableList<UserItem> = _friendRequestsLiveData.value!!.toMutableList()
        if (index == -1) {
            return -1
        }
        if (index != null) {
            newFriends.removeAt(index)
        }
        _friendRequestsLiveData.value = newFriends
        return newFriends.size
    }

    fun acceptPerson(user: UserItem): Int {
        compositeDisposable.add(
            friendshipRepository.acceptNewFriend(user.userId)
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

        val uiData = uiState.value.let {
            if (it is UiState.Loaded) {
                it.data
            } else null
        } ?: return 0

        val friends = uiData.friends.toMutableList()

        val acceptedUserItem = _friendRequestsLiveData.value?.find { it.userId == user.userId }

        val newFriends: MutableList<UserItem> = _friendRequestsLiveData.value!!.toMutableList()

        if (acceptedUserItem != null) {
            newFriends.remove(acceptedUserItem)
            friends.add(acceptedUserItem)
        }

        _friendRequestsLiveData.value = newFriends
        toLoaded(UiData(uiData.userProfile, friends))

        return newFriends.size
    }

    data class UiData(
        val userProfile: UserProfile,
        val friends: List<UserItem>
    )

    companion object {
        const val TAG = "ProfileViewModel_TAG"
    }
}