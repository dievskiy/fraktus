package app.rootstock.data.channel

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface ChannelFavouriteDao {

    @Query("select channels.channel_id, channels.background_color, channels.name, channels.workspace_id, channels.last_message, channels.image_url, channels.last_update from channels_favourite inner join channels on channels_favourite.channel_id = channels.channel_id")
    fun get(): Flow<List<Channel>>

    @Query("insert into channels_favourite (channel_id) values (:channelId)")
    suspend fun add(channelId: Long)

    @Query("delete from channels_favourite where channel_id = :channelId")
    suspend fun remove(channelId: Long)

    @Query("select channels.channel_id, channels.background_color, channels.name, channels.workspace_id, channels.last_message, channels.image_url, channels.last_update from channels_favourite inner join channels on channels_favourite.channel_id = channels.channel_id where channels_favourite.channel_id = :channelId")
    suspend fun getFavourite(channelId: Long): Channel?

    @Query("select count(*) from channels_favourite")
    suspend fun getSize(): Int
}