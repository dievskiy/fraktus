package app.rootstock.di.modules

import app.rootstock.api.MessageService
import app.rootstock.data.db.AppDatabase
import app.rootstock.data.db.RemoteKeysDao
import app.rootstock.data.messages.MessageDao
import app.rootstock.data.messages.MessageRepository
import app.rootstock.data.network.CacheCleaner
import app.rootstock.data.prefs.SharedPrefsController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import retrofit2.Retrofit


/**
 * Module for ChannelActivity
 */
@InstallIn(ActivityComponent::class)
@Module
object ChannelModule {

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }


    @Provides
    fun provideRemoteKeysDao(database: AppDatabase): RemoteKeysDao {
        return database.remoteKeysDao()
    }


    @Provides
    fun provideMessageService(retrofit: Retrofit): MessageService {
        return retrofit.create(MessageService::class.java)
    }


    @ActivityScoped
    @Provides
    fun provideMessageRepository(
        messageDao: MessageDao,
        messageService: MessageService,
        remoteKeysDao: RemoteKeysDao,
        database: AppDatabase,
        spController: SharedPrefsController
    ): MessageRepository {
        return MessageRepository(messageDao, messageService, remoteKeysDao, database, spController)
    }

}
