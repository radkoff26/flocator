package ru.flocator.app.di.modules.app

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.flocator.data.database.ApplicationDatabase
import ru.flocator.data.database.dao.MarkDao
import ru.flocator.data.database.dao.MarkPhotoDao
import ru.flocator.data.database.dao.UserDao
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideApplicationDatabase(context: Context): ApplicationDatabase =
        Room.databaseBuilder(
            context,
            ApplicationDatabase::class.java,
            APPLICATION_DATABASE
        ).build()

    @Provides
    @Singleton
    fun provideMarkDao(applicationDatabase: ApplicationDatabase): MarkDao =
        applicationDatabase.markDao()

    @Provides
    @Singleton
    fun provideMarkPhotoDao(applicationDatabase: ApplicationDatabase): MarkPhotoDao =
        applicationDatabase.markPhotoDao()

    @Provides
    @Singleton
    fun provideUserDao(applicationDatabase: ApplicationDatabase): UserDao =
        applicationDatabase.userDao()

    private const val APPLICATION_DATABASE = "application_database"
}