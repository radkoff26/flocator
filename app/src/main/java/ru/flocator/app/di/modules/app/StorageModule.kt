package ru.flocator.app.di.modules.app

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.flocator.data.preferences.LanguagePreferences
import ru.flocator.data.token.TokenPreferences
import ru.flocator.map.api.MapPreferences
import javax.inject.Singleton

@Module
object StorageModule {

    @Provides
    @Singleton
    fun provideLanguagePreferences(context: Context): LanguagePreferences = LanguagePreferences(context)

    @Provides
    @Singleton
    fun provideMapPreferences(context: Context): MapPreferences = MapPreferences(context)

    @Provides
    @Singleton
    fun provideTokenPreferences(context: Context): TokenPreferences = TokenPreferences(context)
}