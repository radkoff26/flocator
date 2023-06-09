package ru.flocator.app.common.storage.db.dao

import androidx.room.*
import ru.flocator.app.common.storage.db.entities.User
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAllFriends(): Single<List<User>>
    @Query("DELETE FROM user")
    @Transaction
    fun clearAll(): Completable
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    fun insertAll(users: List<User>): Completable
    fun updateTable(users: List<User>): Completable {
        return Completable.concatArray(
            clearAll(),
            insertAll(users)
        )
    }
}