package com.example.flocator.community.api


import com.example.flocator.community.data_classes.User
import com.example.flocator.community.data_classes.UserExternal
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    @GET("user/{userId}")
    fun getUserSolo(@Path("userId") userId: Long): Single<User>

    /*@GET("friendship/located")
    fun getUserFriends(@Query("userId") userId: Long): Single<List<User>>*/

    @GET("user/target/{userId}")
    fun getUser(@Path("userId") userId: Long): Single<User>

    @GET("user/external/{userId}")
    fun getUserExternal(@Path("userId") userId: Long, @Query("targetUserId") targetUserId: Long): Single<UserExternal>

    @POST("friendship/add_by_login")
    fun addNewFriendByLogin(@Query("userId") userId: Long, @Query("login") login: String): Completable

    @POST("friendship/reject")
    fun rejectNewFriend(@Query("userId") userId: Long, @Query("friendId") friendId: Long): Completable

    @POST("friendship/accept")
    fun acceptNewFriend(@Query("userId") userId: Long, @Query("friendId") friendId: Long): Completable

    @POST("friendship/add")
    fun addNewFriend(@Query("userId") userId: Long, @Query("friendId") friendId: Long): Completable


}