package ru.flocator.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.flocator.app.di.annotations.BaseApi
import ru.flocator.app.di.annotations.DependencyKey
import ru.flocator.app.di.annotations.GeocoderApi
import ru.flocator.app.main.internal.main.domain.address.AddressDeserializer
import ru.flocator.core_client.*
import ru.flocator.core_config.Constants
import ru.flocator.core_dto.address.AddressResponse
import javax.inject.Singleton

@Module
@Singleton
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
    @IntoMap
    @DependencyKey(ClientAPI::class)
    fun provideClientAPI(@BaseApi retrofit: Retrofit): ClientAPI = retrofit.create()

    @Provides
    @Singleton
    @IntoMap
    @DependencyKey(SettingsAPI::class)
    fun provideSettingsAPI(@BaseApi retrofit: Retrofit): SettingsAPI = retrofit.create()

    @Provides
    @Singleton
    @IntoMap
    @DependencyKey(GeocoderAPI::class)
    fun provideGeocoderAPI(@GeocoderApi retrofit: Retrofit): GeocoderAPI = retrofit.create()

    @Provides
    @Singleton
    @IntoMap
    @DependencyKey(UserApi::class)
    fun provideUserAPI(@BaseApi retrofit: Retrofit): UserApi = retrofit.create()

    @Provides
    @Singleton
    @IntoMap
    @DependencyKey(AuthenticationApi::class)
    fun provideAuthenticationApi(@BaseApi retrofit: Retrofit): AuthenticationApi = retrofit.create()
}