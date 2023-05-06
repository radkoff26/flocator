package com.example.flocator.main.api

import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.main.models.dto.MarkDto
import com.example.flocator.main.models.dto.UserLocationDto
import com.example.flocator.common.storage.storage.user.info.UserInfo
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
}
