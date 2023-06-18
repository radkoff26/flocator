package ru.flocator.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.flocator.core_database.ApplicationDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    private const val APPLICATION_DATABASE = "application_database"

    @Provides
    @Singleton
    fun applicationDatabase(@ApplicationContext context: Context): ApplicationDatabase {
        return Room.databaseBuilder(
            context,
            ApplicationDatabase::class.java,
            APPLICATION_DATABASE
        ).build()
    }
}