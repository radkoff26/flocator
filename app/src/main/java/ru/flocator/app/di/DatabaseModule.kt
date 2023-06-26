package ru.flocator.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.flocator.app.di.annotations.DependencyKey
import ru.flocator.core_database.ApplicationDatabase
import javax.inject.Singleton

@Module
@Singleton
class DatabaseModule {

    @Provides
    @Singleton
    @IntoMap
    @DependencyKey(ApplicationDatabase::class)
    fun applicationDatabase(context: Context): ApplicationDatabase =
        Room.databaseBuilder(
            context,
            ApplicationDatabase::class.java,
            APPLICATION_DATABASE
        ).build()

    companion object {
        private const val APPLICATION_DATABASE = "application_database"
    }
}