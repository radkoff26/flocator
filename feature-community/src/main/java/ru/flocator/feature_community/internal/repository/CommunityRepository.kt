package ru.flocator.feature_community.internal.repository

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_dto.user.TargetUser
import ru.flocator.feature_community.internal.data_source.UserAPI
import javax.inject.Inject

internal class CommunityRepository @Inject constructor(
    private val userApi: UserAPI
) {

    fun getUser(userId: Long): Single<TargetUser>{
        return userApi.getUser(userId).subscribeOn(Schedulers.io())
    }
    fun addFriendByLogin(userId: Long, login: String): Completable {
        return userApi.addNewFriendByLogin(userId, login).subscribeOn(Schedulers.io())
    }

    fun rejectNewFriend(userId: Long, friendId: Long): Completable {
        return userApi.rejectNewFriend(userId, friendId).subscribeOn(Schedulers.io())
    }

    fun acceptNewFriend(userId: Long, friendId: Long): Completable {
        return userApi.acceptNewFriend(userId, friendId).subscribeOn(Schedulers.io())
    }

    fun addNewFriendByBtn(userId: Long, friendId: Long): Completable {
        return userApi.addNewFriend(userId, friendId).subscribeOn(Schedulers.io())
    }

    fun deleteFriendByBtn(userId: Long, friendId: Long): Completable {
        return userApi.deleteFriend(userId, friendId).subscribeOn(Schedulers.io())
    }

    fun blockUserByBtn(blockerId: Long, blockedId: Long): Completable {
        return userApi.blockUser(blockerId, blockedId).subscribeOn(Schedulers.io())
    }

    fun unblockUserByBtn(blockerId: Long, blockedId: Long): Completable {
        return userApi.unblockUser(blockerId, blockedId).subscribeOn(Schedulers.io())
    }

    fun checkLogin(login: String): Single<Boolean> {
        return userApi.isLoginAvailable(login).subscribeOn(Schedulers.io())
    }
}