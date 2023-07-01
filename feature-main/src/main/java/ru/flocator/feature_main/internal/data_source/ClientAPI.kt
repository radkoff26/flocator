package ru.flocator.feature_main.internal.data_source

import androidx.constraintlayout.solver.widgets.analyzer.Dependency
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.feature_main.internal.domain.location.UserLocationDto
import ru.flocator.feature_main.internal.domain.mark.MarkDto
import ru.flocator.feature_main.internal.domain.user_name.UsernameDto

internal interface ClientAPI: Dependency {

    @GET("friendship/located")
    fun getUserFriendsLocated(@Query("userId") userId: Long): Single<List<ru.flocator.core_database.entities.User>>

    @GET("mark/friends")
    fun getUserAndFriendsMarks(@Query("userId") userId: Long): Single<List<MarkDto>>

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
    ): Single<MarkDto>

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
