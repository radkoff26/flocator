package ru.flocator.app.di.modules.app

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.flocator.core.cache.global.PhotoCacheManager
import ru.flocator.core.cache.global.PhotoLoader
import javax.inject.Singleton

@Module
object PhotoCacheModule {

    @Provides
    @Singleton
    fun providePhotoCacheManager(context: Context): PhotoCacheManager = PhotoCacheManager(context)

    @Provides
    @Singleton
    fun providePhotoLoader(manager: PhotoCacheManager): PhotoLoader = PhotoLoader(manager)
}