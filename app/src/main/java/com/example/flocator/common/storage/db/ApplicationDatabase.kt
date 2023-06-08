package com.example.flocator.common.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.flocator.common.storage.db.entities.Mark
import com.example.flocator.common.storage.db.entities.MarkPhoto
import com.example.flocator.common.storage.db.dao.MarkDao
import com.example.flocator.common.storage.db.dao.MarkPhotoDao
import com.example.flocator.common.storage.db.dao.UserDao
import com.example.flocator.common.storage.db.entities.User

@Database(entities = [Mark::class, MarkPhoto::class, User::class], version = 1)
@TypeConverters(value = [LatLngConverter::class, TimestampConverter::class])
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun markDao(): MarkDao
    abstract fun markPhotoDao(): MarkPhotoDao
    abstract fun userDao(): UserDao
}