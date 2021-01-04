package app.rootstock.data.version

import androidx.room.*

@Dao
interface VersionDao {

    @Query("select * from version limit 1")
    suspend fun get(): Version?

    @Insert
    suspend fun insert(version: Version?)

    @Transaction
    suspend fun deleteAndInsert(version: Version?) {
        if (version != null) {
            deleteAll()
            insert(version)
        }
    }

    @Query("delete from version")
    suspend fun deleteAll()

}