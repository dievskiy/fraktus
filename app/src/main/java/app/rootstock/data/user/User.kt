package app.rootstock.data.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

interface UserWithPassword {
    val email: String
    val password: String
}

data class DeleteUser constructor(val email: String)

/**
 * User class
 */
@Entity(tableName = "users")
class User(
    @PrimaryKey @SerializedName("user_id")
    @ColumnInfo(name = "user_id")
    val userId: String,
    val email: String
)

