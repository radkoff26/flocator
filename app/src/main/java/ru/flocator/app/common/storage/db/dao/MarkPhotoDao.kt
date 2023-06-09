package ru.flocator.app.common.storage.db.dao

import androidx.room.*
import ru.flocator.app.common.storage.db.entities.MarkPhoto
import io.reactivex.Completable

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