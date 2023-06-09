package ru.flocator.app.common.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.flocator.app.common.storage.db.entities.Mark
import ru.flocator.app.common.storage.db.entities.MarkPhoto
import ru.flocator.app.common.storage.db.dao.MarkDao
import ru.flocator.app.common.storage.db.dao.MarkPhotoDao
import ru.flocator.app.common.storage.db.dao.UserDao
import ru.flocator.app.common.storage.db.entities.User

@Database(entities = [Mark::class, MarkPhoto::class, User::class], version = 1)
@TypeConverters(value = [LatLngConverter::class, TimestampConverter::class])
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun markDao(): MarkDao
    abstract fun markPhotoDao(): MarkPhotoDao
    abstract fun userDao(): UserDao
}