package ru.flocator.app.di.modules.app

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.flocator.core_database.ApplicationDatabase
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun applicationDatabase(context: Context): ApplicationDatabase =
        Room.databaseBuilder(
            context,
            ApplicationDatabase::class.java,
            APPLICATION_DATABASE
        ).build()

    private const val APPLICATION_DATABASE = "application_database"
}