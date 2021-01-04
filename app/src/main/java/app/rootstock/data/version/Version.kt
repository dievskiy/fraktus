package app.rootstock.data.version

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "version")
data class Version(@PrimaryKey @SerializedName("current_version") val version: Int)