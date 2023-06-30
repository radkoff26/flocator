package ru.flocator.feature_community.internal.data_source

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*
import ru.flocator.core_dto.user.TargetUser
import ru.flocator.core_dto.user.UserExternal

internal interface UserAPI {

    @GET("user/target/{userId}")
    fun getUser(@Path("userId") userId: Long): Single<TargetUser>

    @GET("user/external/{userId}")
    fun getUserExternal(
        @Path("userId") userId: Long,
        @Query("targetUserId") targetUserId: Long
    ): Single<UserExternal>

    @POST("friendship/add_by_login")
    fun addNewFriendByLogin(
        @Query("userId") userId: Long,
        @Query("login") login: String
    ): Completable

    @POST("friendship/reject")
    fun rejectNewFriend(
        @Query("userId") userId: Long,
        @Query("friendId") friendId: Long
    ): Completable

    @POST("friendship/accept")
    fun acceptNewFriend(
        @Query("userId") userId: Long,
        @Query("friendId") friendId: Long
    ): Completable

    @POST("friendship/add")
    fun addNewFriend(@Query("userId") userId: Long, @Query("friendId") friendId: Long): Completable

    @DELETE("friendship")
    fun deleteFriend(@Query("userId") userId: Long, @Query("friendId") friendId: Long): Completable

    @POST("user/block")
    fun blockUser(
        @Query("blockerId") blockerId: Long,
        @Query("blockedId") blockedId: Long
    ): Completable

    @POST("user/unblock")
    fun unblockUser(
        @Query("blockerId") blockerId: Long,
        @Query("blockedId") blockedId: Long
    ): Completable

    @GET("user/is_login_available")
    fun isLoginAvailable(@Query("login") login: String): Single<Boolean>
}