package ru.flocator.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.flocator.app.di.annotations.DependencyKey
import ru.flocator.cache.global.PhotoCacheManager
import javax.inject.Singleton

@Module
@Singleton
class CacheModule {

    @Provides
    @Singleton
    @IntoMap
    @DependencyKey(PhotoCacheManager::class)
    fun providePhotoCacheManager(context: Context): PhotoCacheManager = PhotoCacheManager(context)
}