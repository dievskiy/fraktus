package app.rootstock.data.messages

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<Message>)

    @Query("SELECT * FROM messages where channel_id = :channelId order by created_at desc")
    fun messagesInChannel(channelId: Long): PagingSource<Int, Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Query("delete from messages where message_id = :id")
    suspend fun delete(id: Long)

    @Query("update messages set content = :content where message_id = :id")
    suspend fun update(id: Long, content: String)

}