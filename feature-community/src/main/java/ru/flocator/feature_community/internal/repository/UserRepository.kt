package ru.flocator.feature_community.internal.repository

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.extensions.withCacheRequest
import ru.flocator.data.data_store.info.UserInfoMediator
import ru.flocator.data.database.dao.UserDao
import ru.flocator.feature_community.internal.data.UserItem
import ru.flocator.feature_community.internal.data.UserProfile
import ru.flocator.feature_community.internal.data_source.UserDataSource
import javax.inject.Inject

internal class UserRepository @Inject constructor(
    private val userDataSource: UserDataSource,
    private val userDao: UserDao,
    private val userInfoMediator: UserInfoMediator
) {

    fun getUserProfileInfo(): Observable<UserProfile> =
        userDataSource.getUser()
            .withCacheRequest(formUserProfileRequestFromCache())
            .subscribeOn(Schedulers.io())

    fun getExternalUserProfileInfo(userId: Long): Single<UserProfile> =
        userDataSource.getExternalUser(userId).subscribeOn(Schedulers.io())

    fun getFriendsOfUser(userId: Long): Single<List<UserItem>> =
        userDataSource.getFriendsOfUser(userId).subscribeOn(Schedulers.io())

    fun getFriendsOfUser(): Observable<List<UserItem>> =
        // TODO: consider updating database
        userDataSource.getFriendsOfUser()
            .withCacheRequest(formFriendsRequestFromCache())
            .subscribeOn(Schedulers.io())

    fun getFriendRequests(): Single<List<UserItem>> =
        userDataSource.getFriendsRequests().subscribeOn(Schedulers.io())

    private fun formUserProfileRequestFromCache(): Single<UserProfile> =
        userInfoMediator.getUserInfo()
            .map {
                UserProfile(
                    it.userId,
                    it.firstName,
                    it.lastName,
                    it.avatarUri,
                    true,
                    null,
                    null
                )
            }

    private fun formFriendsRequestFromCache(): Single<List<UserItem>> =
        userDao.getAllFriends()
            .map {
                it.map { user ->
                    UserItem(
                        user.userId,
                        user.firstName,
                        user.lastName,
                        user.avatarUri
                    )
                }
            }
}