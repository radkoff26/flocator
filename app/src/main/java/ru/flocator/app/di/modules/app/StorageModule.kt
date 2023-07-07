package ru.flocator.app.di.modules.app

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.flocator.cache.storage.SettingsStorage
import ru.flocator.cache.storage.SettingsStorageImpl
import javax.inject.Singleton

@Module
object StorageModule {

    @Provides
    @Singleton
    fun provideSettingsStorage(context: Context): SettingsStorage = SettingsStorageImpl(context)
}