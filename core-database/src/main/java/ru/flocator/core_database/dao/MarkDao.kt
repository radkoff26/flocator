package ru.flocator.core_database.dao

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single
import ru.flocator.core_database.entities.Mark
import ru.flocator.core_database.entities.MarkWithPhotos

@Dao
interface MarkDao {
    @Query("SELECT * FROM mark")
    @Transaction
    fun getAllMarks(): Single<List<MarkWithPhotos>>
    @Query("DELETE FROM mark")
    fun clearAll(): Completable
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMarks(marks: List<Mark>): Completable
    fun updateTable(marks: List<Mark>): Completable {
        return Completable.concatArray(
            clearAll(),
            insertMarks(marks)
        )
    }
}
