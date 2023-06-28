package ru.flocator.core_client

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_dependency.Dependencies

interface ClientAPI: Dependencies {
    @GET("friendship/located")
    fun getUserFriendsLocated(@Query("userId") userId: Long): Single<List<ru.flocator.core_database.entities.User>>

    @GET("mark/friends")
    fun getUserAndFriendsMarks(@Query("userId") userId: Long): Single<List<ru.flocator.core_dto.mark.MarkDto>>

    @Multipart
    @POST("mark")
    fun postMark(
        @Part("mark") markDto: RequestBody,
        @Part photos: List<MultipartBody.Part>
    ): Completable

    @GET("mark/{markId}")
    fun getMark(
        @Path("markId") markId: Long,
        @Query("userId") userId: Long
    ): Single<ru.flocator.core_dto.mark.MarkDto>

    @GET("user/{userId}")
    fun getUser(@Path("userId") userId: Long): Single<UserInfo>

    @POST("user/location")
    fun updateLocation(@Body userLocationDto: ru.flocator.core_dto.location.UserLocationDto): Completable

    @POST("mark/like")
    fun likeMark(@Query("markId") markId: Long, @Query("userId") userId: Long): Completable

    @POST("mark/unlike")
    fun unlikeMark(@Query("markId") markId: Long, @Query("userId") userId: Long): Completable

    @GET("user/username/{userId}")
    fun getUsername(@Path("userId") userId: Long): Single<ru.flocator.core_dto.user_name.UsernameDto>

    @POST("user/online")
    fun goOnline(@Query("userId") userId: Long): Completable

    @POST("user/offline")
    fun goOffline(@Query("userId") userId: Long): Completable
}
