package ru.flocator.app.data_source

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.flocator.app.data.TokenPair

interface AccessRefreshDataSource {

    @GET("auth/login")
    fun login(@Query("username") login: String, @Query("password") password: String): Call<String>

    @GET("auth/refresh")
    fun refreshTokens(@Query("token") refreshToken: String): Call<TokenPair>
}