package app.rootstock.data.user

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {

    /**
     * We want only one user to be in db
     */
    @Transaction
    suspend fun deleteAndInsert(user: User) {
        deleteAll()
        insert(user)
    }

    @Query("select * from users limit 1")
    fun searchUser(): LiveData<User?>

    @Query("select user_id from users limit 1")
    suspend fun getUserId(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("delete from users")
    suspend fun deleteAll()
}