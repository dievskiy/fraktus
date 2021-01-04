package app.rootstock.data.token

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.rootstock.data.db.DateConverter
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "token", primaryKeys = ["access_token"])
data class Token constructor(
    @ColumnInfo(name = "access_token") @SerializedName("access_token") val accessToken: String,
    @ColumnInfo(name = "refresh_token") @SerializedName("refresh_token") val refreshToken: String,
    @ColumnInfo(name = "token_type") @SerializedName("token_type") val tokenType: String,
)

data class TokenUpdate constructor(
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user_id") val userId: String,
)

data class TokenRevoke constructor(
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user_id") val userId: String,
)