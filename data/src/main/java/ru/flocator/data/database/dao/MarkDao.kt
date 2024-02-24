package ru.flocator.data.database.dao

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single
import ru.flocator.data.database.entities.Mark
import ru.flocator.data.database.entities.MarkWithPhotos

@Dao
interface MarkDao {
    @Query("SELECT * FROM mark")
    @Transaction
    fun getAllMarks(): Single<List<MarkWithPhotos>>

    @Query("DELETE FROM mark")
    @Transaction
    fun clearAll(): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    fun insertMarks(marks: List<Mark>): Completable
}
