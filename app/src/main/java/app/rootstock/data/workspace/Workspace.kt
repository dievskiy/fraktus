package app.rootstock.data.workspace

import androidx.room.*
import app.rootstock.data.channel.Channel
import app.rootstock.data.db.DateConverter
import app.rootstock.data.user.User
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

interface WorkspaceI : Serializable {
    val workspaceId: String
    val name: String
    val imageUrl: String?
    val backgroundColor: String
    val createdAt: Date
}

@Entity(
    tableName = "workspaces",
    indices = [Index("ws_id")],
)
@TypeConverters(DateConverter::class)
data class Workspace(
    @PrimaryKey
    @ColumnInfo(name = "ws_id")
    @SerializedName("ws_id")
    override val workspaceId: String,
    override var name: String,
    @ColumnInfo(name = "background_color")
    @SerializedName("background_color")
    override val backgroundColor: String,
    @ColumnInfo(name = "image_url")
    @SerializedName("image_url")
    override val imageUrl: String?,
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    override val createdAt: Date
) : WorkspaceI

/**
 * This class represents 1:m relationship in Workspace - Channels tables (Needed for Room)
 */
data class WorkspaceWithChannels(
    @Embedded val workspace: Workspace,
    @Relation(
        parentColumn = "ws_id",
        entityColumn = "workspace_id",
    )
    val channels: List<Channel>,
)


data class WorkspaceWithChildren(
    override var name: String,
    @SerializedName("background_color")
    override val backgroundColor: String,
    @SerializedName("image_url")
    override var imageUrl: String?,
    @SerializedName("ws_id")
    override val workspaceId: String,
    @SerializedName("created_at")
    override val createdAt: Date,
    var channels: List<Channel>,
    var children: MutableList<Workspace>,
) : WorkspaceI


object WorkspaceConstants{
    private const val workspaceNameMaxLength = 50
    val workspaceNameRange = (1..workspaceNameMaxLength)
}