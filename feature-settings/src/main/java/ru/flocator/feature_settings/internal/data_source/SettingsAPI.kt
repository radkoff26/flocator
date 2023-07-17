package ru.flocator.feature_settings.internal.data_source

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.*
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.feature_settings.internal.domain.privacy.PrivacyData
import ru.flocator.feature_settings.internal.domain.privacy.PrivacyType
import java.sql.Timestamp

internal interface SettingsAPI {
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
    fun changeAvatar(
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
    fun getBlocked(@Path("userId") userId: Long): Single<List<ru.flocator.core_data_store.user.info.UserInfo>>

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

    @GET("friendship/privacy")
    fun getPrivacyData(
        @Query("userId") userId: Long
    ): Single<List<PrivacyData>>

    @POST("friendship/privacy/change")
    fun changePrivacyData(
        @Query("userId") userId: Long,
        @Query("friendId") friendId: Long,
        @Query("status") privacyType: PrivacyType
    ): Completable

    @DELETE("user")
    fun deleteAccount(
        @Query("userId") userId: Long,
        @Query("password") password: String
    ): Completable

    @GET("friendship/located")
    fun getUserFriendsLocated(@Query("userId") userId: Long): Single<List<ru.flocator.core_database.entities.User>>

    @GET("user/{userId}")
    fun getUser(@Path("userId") userId: Long): Single<UserInfo>
}