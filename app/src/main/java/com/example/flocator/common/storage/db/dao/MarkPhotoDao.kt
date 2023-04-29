package com.example.flocator.common.storage.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.flocator.common.storage.db.entities.MarkPhoto
import io.reactivex.Completable

@Dao
interface MarkPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotos(photos: List<MarkPhoto>): Completable
}