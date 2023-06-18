package ru.flocator.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.flocator.cache.global.PhotoCacheManager

@InstallIn(SingletonComponent::class)
@Module
object CacheModule {

    @Provides
    fun providePhotoCacheManager(@ApplicationContext context: Context): PhotoCacheManager =
        PhotoCacheManager(context)
}