package app.rootstock.data.messages

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import app.rootstock.data.channel.Channel
import app.rootstock.data.db.DateConverter
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Channel::class,
            childColumns = ["channel_id"],
            parentColumns = ["channel_id"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index("channel_id"),
    ]
)
@TypeConverters(DateConverter::class)
data class Message constructor(
    @PrimaryKey
    @field:SerializedName("message_id") @ColumnInfo(name = "message_id") val messageId: Long,
    @field:SerializedName("content") val content: String,
    @field:SerializedName("created_at") @ColumnInfo(name = "created_at") val createdAt: Date,
    @field:SerializedName("message_type") val type: Short,
    @field:SerializedName("channel_id") @ColumnInfo(name = "channel_id") var channelId: Long,
)