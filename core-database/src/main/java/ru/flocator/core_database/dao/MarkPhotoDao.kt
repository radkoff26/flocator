package ru.flocator.core_database.dao

import androidx.room.*
import io.reactivex.Completable
import ru.flocator.core_database.entities.MarkPhoto

@Dao
interface MarkPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotos(photos: List<MarkPhoto>): Completable
    @Query("DELETE FROM mark_photo")
    fun clearAll(): Completable
    fun updateTable(photos: List<MarkPhoto>): Completable {
        return Completable.concatArray(
            clearAll(),
            insertPhotos(photos)
        )
    }
}