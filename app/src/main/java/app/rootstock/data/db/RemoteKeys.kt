package app.rootstock.data.db


import androidx.room.*
import app.rootstock.data.channel.Channel

@Entity(
    tableName = "remote_keys",
    foreignKeys = [
        ForeignKey(
            entity = Channel::class,
            childColumns = ["channel_id"],
            parentColumns = ["channel_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("message_id"),
        Index("channel_id"),
    ]
)
data class RemoteKeys(
    @PrimaryKey @ColumnInfo(name = "message_id") val messageId: Long,
    @ColumnInfo(name = "prev_key") val prevKey: Int?,
    @ColumnInfo(name = "next_key") val nextKey: Int?,
    @ColumnInfo(name = "channel_id") val channelId: Long,
)
