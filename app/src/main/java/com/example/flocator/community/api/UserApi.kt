package com.example.flocator.community.api


import com.example.flocator.community.data_classes.User
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    @GET("user/{userId}")
    fun getUser(@Path("userId") userId: Long): Single<User>
}