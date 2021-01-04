package app.rootstock.data.db


import androidx.room.*
import app.rootstock.data.channel.Channel

@Dao
interface RemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(remoteKey: List<RemoteKeys>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(entities: List<RemoteKeys>)

    @Transaction
    suspend fun upsertAll(entities: List<RemoteKeys>) {
        insertAll(entities)
        update(entities)
    }

    @Query("SELECT * FROM remote_keys WHERE channel_id = :channelId and message_id = :messageId")
    suspend fun remoteKeysMessageId(messageId: Long, channelId: Long): RemoteKeys?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(remoteKey: RemoteKeys)

    @Query("SELECT * FROM remote_keys WHERE message_id in (select max(message_id) from remote_keys where channel_id = :channelId)")
    fun getLastRemoteKeys(channelId: Long): RemoteKeys?

    @Query("select count(*) from remote_keys where channel_id = :channelId")
    fun getRemoteKeysCount(channelId: Long): Int
}

