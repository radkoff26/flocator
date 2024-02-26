package ru.flocator.feature_community.internal.data.repository

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.flocator.feature_community.internal.data.data_source.UserDataSource
import javax.inject.Inject

internal class FriendshipRepository @Inject constructor(
    private val userDataSource: UserDataSource
) {

    fun addFriendByLogin(login: String): Completable {
        return userDataSource.addNewFriendByLogin(login).subscribeOn(Schedulers.io())
    }

    fun rejectNewFriend(friendId: Long): Completable {
        return userDataSource.rejectNewFriend(friendId).subscribeOn(Schedulers.io())
    }

    fun acceptNewFriend(friendId: Long): Completable {
        return userDataSource.acceptNewFriend(friendId).subscribeOn(Schedulers.io())
    }

    fun addNewFriend(friendId: Long): Single<Boolean> {
        return userDataSource.addNewFriend(friendId).subscribeOn(Schedulers.io())
    }

    fun deleteFriend(friendId: Long): Completable {
        return userDataSource.deleteFriend(friendId).subscribeOn(Schedulers.io())
    }

    fun blockUser(blockedId: Long): Completable {
        return userDataSource.blockUser(blockedId).subscribeOn(Schedulers.io())
    }

    fun unblockUser(blockedId: Long): Completable {
        return userDataSource.unblockUser(blockedId).subscribeOn(Schedulers.io())
    }

    fun checkLogin(login: String): Single<Boolean> {
        return userDataSource.isLoginAvailable(login).subscribeOn(Schedulers.io())
    }
}