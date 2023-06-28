package ru.flocator.core_database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.flocator.core_database.dao.MarkDao
import ru.flocator.core_database.dao.MarkPhotoDao
import ru.flocator.core_database.dao.UserDao
import ru.flocator.core_database.entities.Mark
import ru.flocator.core_database.entities.MarkPhoto
import ru.flocator.core_database.entities.User
import ru.flocator.core_dependency.Dependencies

@Database(entities = [Mark::class, MarkPhoto::class, User::class], version = 1)
@TypeConverters(value = [LatLngConverter::class, TimestampConverter::class])
abstract class ApplicationDatabase : RoomDatabase(), Dependencies {
    abstract fun markDao(): MarkDao
    abstract fun markPhotoDao(): MarkPhotoDao
    abstract fun userDao(): UserDao
}