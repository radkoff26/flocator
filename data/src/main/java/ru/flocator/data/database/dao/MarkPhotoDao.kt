package ru.flocator.data.database.dao

import androidx.room.*
import io.reactivex.Completable
import ru.flocator.data.database.entities.MarkPhoto

@Dao
interface MarkPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    fun insertPhotos(photos: List<MarkPhoto>): Completable

    @Query("DELETE FROM mark_photo")
    @Transaction
    fun clearAll(): Completable
}