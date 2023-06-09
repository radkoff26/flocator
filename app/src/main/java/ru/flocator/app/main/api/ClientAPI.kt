package ru.flocator.app.main.api

import ru.flocator.app.common.storage.db.entities.User
import ru.flocator.app.main.models.dto.MarkDto
import ru.flocator.app.main.models.dto.UserLocationDto
import ru.flocator.app.common.storage.store.user.info.UserInfo
import ru.flocator.app.main.models.dto.UsernameDto
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientAPI {
    @GET("friendship/located")
    fun getUserFriendsLocated(@Query("userId") userId: Long): Single<List<User>>

    @GET("mark/friends")
    fun getUserAndFriendsMarks(@Query("userId") userId: Long): Single<List<MarkDto>>

    @Multipart
    @POST("mark")
    fun postMark(
        @Part("mark") markDto: RequestBody,
        @Part photos: List<MultipartBody.Part>
    ): Completable

    @GET("mark/{markId}")
    fun getMark(@Path("markId") markId: Long, @Query("userId") userId: Long): Single<MarkDto>

    @GET("user/{userId}")
    fun getUser(@Path("userId") userId: Long): Single<UserInfo>

    @POST("user/location")
    fun updateLocation(@Body userLocationDto: UserLocationDto): Completable

    @POST("mark/like")
    fun likeMark(@Query("markId") markId: Long, @Query("userId") userId: Long): Completable

    @POST("mark/unlike")
    fun unlikeMark(@Query("markId") markId: Long, @Query("userId") userId: Long): Completable

    @GET("user/username/{userId}")
    fun getUsername(@Path("userId") userId: Long): Single<UsernameDto>

    @POST("user/online")
    fun goOnline(@Query("userId") userId: Long): Completable

    @POST("user/offline")
    fun goOffline(@Query("userId") userId: Long): Completable
}
