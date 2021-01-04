package app.rootstock.di.modules

import app.rootstock.api.ChannelService
import app.rootstock.api.VersionService
import app.rootstock.api.WorkspaceService
import app.rootstock.data.channel.ChannelDao
import app.rootstock.data.channel.ChannelFavouriteDao
import app.rootstock.data.db.AppDatabase
import app.rootstock.data.network.CacheCleaner
import app.rootstock.data.prefs.SharedPrefsController
import app.rootstock.data.version.VersionDao
import app.rootstock.data.workspace.WorkspaceDao
import app.rootstock.ui.channels.ChannelRepository
import app.rootstock.ui.channels.ChannelRepositoryImpl
import app.rootstock.ui.channels.favourites.ChannelFavouriteRepository
import app.rootstock.ui.channels.favourites.ChannelFavouriteRepositoryImpl
import app.rootstock.ui.workspace.VersionRepository
import app.rootstock.ui.workspace.VersionRepositoryImpl
import app.rootstock.ui.workspace.WorkspaceRepository
import app.rootstock.ui.workspace.WorkspaceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import retrofit2.Retrofit
import javax.inject.Singleton


/**
 * Module for WorkspaceActivity
 */
@InstallIn(ActivityComponent::class)
@Module
object WorkspaceModule {

    @Provides
    fun provideWorkspaceDao(appDatabase: AppDatabase): WorkspaceDao {
        return appDatabase.workspaceDao()
    }

    @Provides
    fun provideChannelDao(appDatabase: AppDatabase): ChannelDao {
        return appDatabase.channelDao()
    }

    @Provides
    fun provideChannelService(retrofit: Retrofit): ChannelService {
        return retrofit.create(ChannelService::class.java)
    }

    @Provides
    fun provideWorkspaceService(retrofit: Retrofit): WorkspaceService {
        return retrofit.create(WorkspaceService::class.java)
    }

    @Provides
    fun provideVersionService(retrofit: Retrofit): VersionService {
        return retrofit.create(VersionService::class.java)
    }

    @Provides
    fun provideWorkspaceRepository(
        workspaceDataSource: WorkspaceService,
        workspaceDao: WorkspaceDao,
        channelDao: ChannelDao,
        spController: SharedPrefsController,
        appDatabase: AppDatabase
    ): WorkspaceRepository {
        return WorkspaceRepositoryImpl(
            workspaceDataSource,
            workspaceDao,
            channelDao,
            spController,
            appDatabase
        )
    }

    @Provides
    fun provideChannelsFavouriteDao(database: AppDatabase): ChannelFavouriteDao {
        return database.favouritesDao()
    }

    @Provides
    fun provideVersionDao(database: AppDatabase): VersionDao {
        return database.versionDao()
    }

    @Provides
    fun provideChannelRepository(
        channelService: ChannelService,
        channelDao: ChannelDao,
        spController: SharedPrefsController,
    ): ChannelRepository {
        return ChannelRepositoryImpl(channelService, channelDao, spController)
    }

    @Provides
    fun provideVersionRepository(
        versionDao: VersionDao,
        versionService: VersionService
    ): VersionRepository {
        return VersionRepositoryImpl(versionDao, versionService)
    }

    @Provides
    fun provideChannelFavouriteRepository(
        favouriteDao: ChannelFavouriteDao
    ): ChannelFavouriteRepository {
        return ChannelFavouriteRepositoryImpl(favouriteDao)
    }
}