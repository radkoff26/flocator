package com.example.flocator.authentication.client

import com.example.flocator.common.config.Constants.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val httpClient: OkHttpClient
        get() {
            return OkHttpClient.Builder()
                .build()
        }

    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    val authenticationApi: AuthenticationApi = retrofit.create(AuthenticationApi::class.java)
}
