package ru.flocator.app.di.modules.app

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.flocator.app.di.annotations.BaseApi
import ru.flocator.app.di.annotations.GeocoderApi
import ru.flocator.core_client.*
import ru.flocator.core_config.Constants
import ru.flocator.core_dto.address.AddressDeserializer
import ru.flocator.core_dto.address.AddressResponse
import javax.inject.Singleton

@Module
class ApiModule {

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

    @Provides
    @Singleton
    fun provideAuthenticationApi(@BaseApi retrofit: Retrofit): AuthenticationApi = retrofit.create()
}