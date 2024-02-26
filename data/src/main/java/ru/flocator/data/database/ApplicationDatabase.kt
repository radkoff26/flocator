package ru.flocator.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import ru.flocator.data.database.dao.MarkDao
import ru.flocator.data.database.dao.MarkPhotoDao
import ru.flocator.data.database.dao.UserDao
import ru.flocator.data.database.entities.Mark
import ru.flocator.data.database.entities.MarkPhoto
import ru.flocator.data.database.entities.User
import ru.flocator.data.database.internal.CoordinatesConverter
import ru.flocator.data.database.internal.TimestampConverter

@Database(entities = [Mark::class, MarkPhoto::class, User::class], version = 2)
@TypeConverters(value = [CoordinatesConverter::class, TimestampConverter::class])
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun markDao(): MarkDao

    abstract fun markPhotoDao(): MarkPhotoDao

    abstract fun userDao(): UserDao

    fun clearDatabase(): Completable {
        return Completable.concatArray(
            markPhotoDao().clearAll(),
            markDao().clearAll(),
            userDao().clearAll()
        )
    }

    fun updateMarks(
        marks: List<Mark>,
        markPhotos: List<MarkPhoto>
    ): Completable {
        return Completable.concatArray(
            markDao().clearAll(),
            markPhotoDao().clearAll(),
            markDao().insertMarks(marks),
            markPhotoDao().insertPhotos(markPhotos)
        ).subscribeOn(Schedulers.io())
    }
}