package app.rootstock.data.db

import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SupportSQLiteOpenHelper
import app.rootstock.data.channel.ChannelDao
import app.rootstock.data.channel.ChannelFavouriteDao
import app.rootstock.data.messages.MessageDao
import app.rootstock.data.token.TokenDao
import app.rootstock.data.user.UserDao
import app.rootstock.data.workspace.WorkspaceDao
import org.mockito.Mockito

class FakeDatabase : AppDatabase() {
    override fun userDao(): UserDao {
        return Mockito.mock(UserDao::class.java)
    }

    override fun tokenDao(): TokenDao {
        return Mockito.mock(TokenDao::class.java)
    }

    override fun workspaceDao(): WorkspaceDao {
        return Mockito.mock(WorkspaceDao::class.java)
    }

    override fun messageDao(): MessageDao {
        return Mockito.mock(MessageDao::class.java)
    }

    override fun remoteKeysDao(): RemoteKeysDao {
        return Mockito.mock(RemoteKeysDao::class.java)
    }

    override fun channelDao(): ChannelDao {
        return Mockito.mock(ChannelDao::class.java)
    }

    override fun favouritesDao(): ChannelFavouriteDao {
        return Mockito.mock(ChannelFavouriteDao::class.java)
    }

    override fun createOpenHelper(config: DatabaseConfiguration?): SupportSQLiteOpenHelper {
        return Mockito.mock(SupportSQLiteOpenHelper::class.java)
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        return Mockito.mock(InvalidationTracker::class.java)
    }

    override fun clearAllTables() {
    }
}