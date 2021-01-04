package app.rootstock.data.channel

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "channels_favourite",
    foreignKeys = [ForeignKey(
        entity = Channel::class,
        childColumns = ["channel_id"],
        parentColumns = ["channel_id"],
        onDelete = CASCADE
    )],
    indices = [Index("channel_id")]
)
data class ChannelFavourite constructor(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "channel_id") val channelId: Long
)