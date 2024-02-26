package ru.flocator.app.data_source

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.flocator.app.data.TokenPair

interface AccessRefreshDataSource {

    @GET("auth/refresh")
    fun refreshTokens(@Query("token") refreshToken: String): Call<TokenPair>
}