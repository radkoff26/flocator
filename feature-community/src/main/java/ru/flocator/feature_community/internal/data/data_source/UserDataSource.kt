package ru.flocator.feature_community.internal.data.data_source

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*
import ru.flocator.data.api.ApiPaths
import ru.flocator.feature_community.internal.data.model.UserItem
import ru.flocator.feature_community.internal.data.model.UserProfile

internal interface UserDataSource {

    @GET(ApiPaths.USER_GET_USER)
    fun getUser(): Single<UserProfile>

    @GET(ApiPaths.USER_GET_USER)
    fun getExternalUser(
        @Query("userId") userId: Long
    ): Single<UserProfile>

    @POST(ApiPaths.FRIENDSHIP_ADD_BY_LOGIN)
    fun addNewFriendByLogin(
        @Query("login") login: String
    ): Completable

    @POST(ApiPaths.FRIENDSHIP_REJECT)
    fun rejectNewFriend(
        @Query("friendId") friendId: Long
    ): Completable

    @POST(ApiPaths.FRIENDSHIP_ACCEPT)
    fun acceptNewFriend(
        @Query("friendId") friendId: Long
    ): Completable

    @POST(ApiPaths.FRIENDSHIP_ADD)
    fun addNewFriend(
        @Query("friendId") friendId: Long
    ): Single<Boolean>

    @GET(ApiPaths.USER_GET_FRIENDS)
    fun getFriendsOfUser(
        @Query("user") userId: Long? = null
    ): Single<List<UserItem>>

    @GET(ApiPaths.USER_GET_FRIEND_REQUESTS)
    fun getFriendsRequests(): Single<List<UserItem>>

    @DELETE(ApiPaths.FRIENDSHIP_DELETE_FRIEND)
    fun deleteFriend(
        @Query("friendId") friendId: Long
    ): Completable

    @POST(ApiPaths.USER_BLOCK)
    fun blockUser(
        @Query("blockedId") blockedId: Long
    ): Completable

    @POST(ApiPaths.USER_UNBLOCK)
    fun unblockUser(
        @Query("blockedId") blockedId: Long
    ): Completable

    @GET(ApiPaths.AUTH_IS_LOGIN_AVAILABLE)
    fun isLoginAvailable(
        @Query("login") login: String
    ): Single<Boolean>
}