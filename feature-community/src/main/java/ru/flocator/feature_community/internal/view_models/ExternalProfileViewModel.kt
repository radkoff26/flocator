package ru.flocator.feature_community.internal.view_models

import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.base.view_model.BaseViewModel
import ru.flocator.core.holder.LateInitHolder
import ru.flocator.data.data_store.info.UserInfoMediator
import ru.flocator.feature_community.internal.data.UserItem
import ru.flocator.feature_community.internal.data.UserProfile
import ru.flocator.feature_community.internal.repository.FriendshipRepository
import ru.flocator.feature_community.internal.repository.UserRepository
import javax.inject.Inject

internal class ExternalProfileViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository,
    private val userRepository: UserRepository,
    userInfoMediator: UserInfoMediator
) : BaseViewModel<ExternalProfileViewModel.UiData>() {
    private val compositeDisposable = CompositeDisposable()

    private var currentUserId: LateInitHolder<Long> = LateInitHolder()

    private var externalUserId: Long? = null

    init {
        compositeDisposable.add(
            userInfoMediator.getUserInfo()
                .subscribe(
                    {
                        currentUserId.init(it.userId)
                    },
                    {
                        currentUserId.init(null)
                        toFailed(IllegalStateException())
                    }
                )
        )
    }

    suspend fun getCurrentUserId(): Long? = currentUserId.get()

    fun setExternalUserId(userId: Long) {
        externalUserId = userId
    }

    fun addOtherUserToFriend(friendId: Long) {
        compositeDisposable.add(
            friendshipRepository.addNewFriend(friendId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(TAG, "addFriend ERROR", it)
                    }
                )
        )
    }

    fun deleteMyFriend(friendId: Long) {
        compositeDisposable.add(
            friendshipRepository.deleteFriend(friendId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(TAG, "deleteFriend ERROR", it)
                    }
                )
        )
    }


    fun acceptFriend(friendId: Long) {
        compositeDisposable.add(
            friendshipRepository.acceptNewFriend(friendId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(ProfileViewModel.TAG, "acceptFriend ERROR", it)
                    }
                )
        )
    }

    fun block(blockedId: Long) {
        compositeDisposable.add(
            friendshipRepository.blockUser(blockedId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(ProfileViewModel.TAG, "blockUser ERROR", it)
                    }
                )
        )
    }

    fun unblock(blockedId: Long) {
        compositeDisposable.add(
            friendshipRepository.unblockUser(blockedId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(ProfileViewModel.TAG, "unblockUser ERROR", it)
                    }
                )
        )
    }

    fun cancelFriendRequest(friendId: Long) {
        compositeDisposable.add(
            friendshipRepository.rejectNewFriend(friendId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(TAG, "cancelFriendRequest ERROR", it)
                    }
                )
        )
    }


    override fun loadData() {
        val id = requireNotNull(externalUserId) {
            "User Id must not be null when loading data!"
        }
        compositeDisposable.add(
            Single.zip(
                userRepository.getExternalUserProfileInfo(id),
                userRepository.getFriendsOfUser(id)
            ) { profileInfo, friends ->
                UiData(profileInfo, friends)
            }.observeOn(AndroidSchedulers.mainThread()).subscribe(
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

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    data class UiData(
        val userProfile: UserProfile,
        val friends: List<UserItem>
    )

    companion object {
        const val TAG = "ExternalProfileViewModel_TAG"
    }
}