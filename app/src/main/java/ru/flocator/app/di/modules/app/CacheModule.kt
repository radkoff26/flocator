package ru.flocator.app.di.modules.app

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.flocator.cache.global.PhotoCacheManager
import javax.inject.Singleton

@Module
class CacheModule {

    @Provides
    @Singleton
    fun providePhotoCacheManager(context: Context): PhotoCacheManager = PhotoCacheManager(context)
}