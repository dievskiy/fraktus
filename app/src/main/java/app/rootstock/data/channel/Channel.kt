package app.rootstock.data.channel

import androidx.room.*
import app.rootstock.data.db.DateConverter
import app.rootstock.data.workspace.Workspace
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

interface ChannelI {
    val name: String
    val channelId: Long
    val lastMessage: String?
    var workspaceId: String
    val imageUrl: String?
    val backgroundColor: String?
    val lastUpdate: Date
}

@Entity(
    tableName = "channels",
    foreignKeys = [
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["ws_id"],
            childColumns = ["workspace_id"],
            onDelete = ForeignKey.CASCADE
        )],
    indices = [Index("workspace_id")],
)
@TypeConverters(DateConverter::class)
data class Channel(
    override var name: String,
    @PrimaryKey
    @ColumnInfo(name = "channel_id")
    @SerializedName("channel_id")
    override val channelId: Long,
    @ColumnInfo(name = "last_message")
    @SerializedName("last_message")
    override val lastMessage: String?,
    @ColumnInfo(name = "background_color")
    @SerializedName("background_color")
    override var backgroundColor: String,
    @ColumnInfo(name = "image_url")
    @SerializedName("image_url")
    override val imageUrl: String?,
    @ColumnInfo(name = "last_update")
    @SerializedName("last_update")
    override val lastUpdate: Date,
    @ColumnInfo(name = "workspace_id")
    @SerializedName("workspace_id")
    override var workspaceId: String
) : ChannelI, Serializable {

    fun isValid(): Boolean {
        if (name.isBlank()) return false
        return true
    }
}


object ChannelConstants {
    private const val channelNameMaxLength = 50
    const val maxFavouriteChannels = 4
    val channelNameRange = (1..channelNameMaxLength)
}
