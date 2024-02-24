package ru.flocator.data.database.dao

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single
import ru.flocator.data.database.entities.User

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