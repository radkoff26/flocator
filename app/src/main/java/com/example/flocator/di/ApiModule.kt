package com.example.flocator.di

import com.example.flocator.common.config.Constants
import com.example.flocator.main.api.ClientAPI
import com.example.flocator.main.api.GeocoderAPI
import com.example.flocator.settings.SettingsAPI
import com.example.flocator.main.data.response.AddressResponse
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
    @Provides
    @Singleton
    fun provideGson(): Gson =
        GsonBuilder()
            .setLenient()
            .create()

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()


    @Provides
    @Singleton
    fun provideClientAPI(retrofit: Retrofit): ClientAPI = retrofit.create()

    @Provides
    @Singleton
    fun provideSettingsAPI(retrofit: Retrofit): SettingsAPI = retrofit.create()

    @Provides
    @Singleton
    fun provideGeocoderAPI(): GeocoderAPI = Retrofit.Builder()
        .baseUrl(Constants.GEOCODER_URL)
        .addConverterFactory(GsonConverterFactory.create(
            GsonBuilder()
                .registerTypeAdapter(AddressResponse::class.java, AddressDeserializer())
                .setLenient()
                .create()
        ))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build().create()
}