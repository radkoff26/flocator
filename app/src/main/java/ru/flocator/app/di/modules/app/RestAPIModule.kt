package ru.flocator.app.di.modules.app

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.flocator.app.AuthInterceptor
import ru.flocator.app.StatusCodeInterceptor
import ru.flocator.app.data_source.AccessRefreshDataSource
import ru.flocator.core.config.Constants
import javax.inject.Singleton

@Module
object RestAPIModule {

    @Provides
    @Singleton
    fun provideGson(): Gson =
        GsonBuilder()
            .setLenient()
            .create()

    @Provides
    @Singleton
    fun provideRetrofit(
        gson: Gson,
        authInterceptor: AuthInterceptor,
        statusCodeInterceptor: StatusCodeInterceptor
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(statusCodeInterceptor)
                    .build()
            )
            .build()

    @Provides
    @Singleton
    fun provideAccessRefreshDataSource(retrofit: Retrofit): AccessRefreshDataSource =
        retrofit.create()
}