package com.example.flocator.settings

import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
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

}