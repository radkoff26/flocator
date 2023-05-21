package com.example.flocator.di

import com.example.flocator.common.config.Constants
import com.example.flocator.community.api.UserApi
import com.example.flocator.di.annotations.BaseApi
import com.example.flocator.di.annotations.GeocoderApi
import com.example.flocator.main.api.ClientAPI
import com.example.flocator.main.api.GeocoderAPI
import com.example.flocator.settings.SettingsAPI
import com.example.flocator.main.data.AddressResponse
import com.example.flocator.main.deserializers.AddressDeserializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiModule {
    @BaseApi
    @Provides
    @Singleton
    fun provideBaseGson(): Gson =
        GsonBuilder()
            .setLenient()
            .create()

    @GeocoderApi
    @Provides
    @Singleton
    fun provideGeocoderGson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(AddressResponse::class.java, AddressDeserializer())
            .setLenient()
            .create()

    @BaseApi
    @Provides
    @Singleton
    fun provideBaseRetrofit(@BaseApi gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    @GeocoderApi
    @Provides
    @Singleton
    fun provideGeocoderRetrofit(@GeocoderApi gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.GEOCODER_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()


    @Provides
    @Singleton
    fun provideClientAPI(@BaseApi retrofit: Retrofit): ClientAPI = retrofit.create()

    @Provides
    @Singleton
    fun provideSettingsAPI(@BaseApi retrofit: Retrofit): SettingsAPI = retrofit.create()

    @Provides
    @Singleton
    fun provideGeocoderAPI(@GeocoderApi retrofit: Retrofit): GeocoderAPI = retrofit.create()

    @Provides
    @Singleton
    fun provideUserAPI(@BaseApi retrofit: Retrofit): UserApi = retrofit.create()
}