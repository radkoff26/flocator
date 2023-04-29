package com.example.flocator.common.storage.db.dao

import androidx.room.*
import com.example.flocator.common.storage.db.entities.Mark
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface MarkDao {
    @Query("SELECT * FROM mark")
    fun getAllMarks(): Single<List<Mark>>
    @Query("DELETE FROM mark")
    @Transaction
    fun clearAll(): Completable
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    fun insertMarks(marks: List<Mark>): Completable
    fun updateTable(marks: List<Mark>): Completable {
        return Completable.concatArray(
            clearAll(),
            insertMarks(marks)
        )
    }
}
