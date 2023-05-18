package com.example.flocator.settings

import com.example.flocator.common.storage.store.user.info.UserInfo
import com.example.flocator.settings.data_models.PrivacyData
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.*
import java.sql.Timestamp

interface SettingsAPI {
    @POST("user/birthdate")
    fun setBirthDate(
        @Query("userId") userId: Long,
        @Query("birthDate") birthDate: Timestamp
    ): Single<Boolean>

    @POST("user/name")
    fun changeName(
        @Query("userId") userId: Long,
        @Query("firstName") firstName: String,
        @Query("lastName") lastName: String
    ): Single<Boolean>

    @Multipart
    @POST("user/avatar")
    fun changeAvatar (
        @Part("userId") userId: Long,
        @Part photo: MultipartBody.Part
    ): Single<Boolean>

    @POST("user/changePassword")
    fun changePassword(
        @Query("userId") userId: Long,
        @Query("previousPassword") previousPassword: String,
        @Query("newPassword") newPassword: String
    ): Single<Boolean>

    @GET("user/blockedBy/{userId}")
    fun getBlocked(@Path("userId") userId: Long): Single<List<UserInfo>>

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

    @GET("user/friendship/privacy")
    fun getPrivacyData(
        @Query("userId") userId: Long
    ): Single<List<PrivacyData>>
}