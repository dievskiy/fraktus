package app.rootstock.data.channel

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface ChannelDao {
    // select root workspace for user
    @Query("select * from channels where channel_id = :id;")
    fun getChannelById(id: Long): Flow<Channel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channel: Channel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<Channel?>?)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(entities: List<Channel?>?)

    @Transaction
    suspend fun upsertAll(entities: List<Channel?>?) {
        insertAll(entities)
        update(entities)
    }

    @Update
    suspend fun update(channel: Channel)

    @Query("delete from channels where channel_id = :channelId")
    suspend fun deleteChannel(channelId: Long)

}