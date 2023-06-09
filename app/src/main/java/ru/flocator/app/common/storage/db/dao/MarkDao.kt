package ru.flocator.app.common.storage.db.dao

import androidx.room.*
import ru.flocator.app.common.storage.db.entities.Mark
import ru.flocator.app.common.storage.db.entities.MarkWithPhotos
import io.reactivex.Completable
import io.reactivex.Single

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
