package app.rootstock.ui.channels

import app.rootstock.api.ChannelService
import app.rootstock.data.channel.Channel
import app.rootstock.data.channel.ChannelDao
import app.rootstock.data.channel.CreateChannelRequest
import app.rootstock.data.network.ResponseResult
import app.rootstock.data.prefs.CacheClass
import app.rootstock.data.prefs.SharedPrefsController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface ChannelRepository {
    suspend fun updateChannel(channel: Channel): Flow<ResponseResult<Channel?>>
    suspend fun deleteChannel(channelId: Long, workspaceId: String): Flow<ResponseResult<Void?>>
    suspend fun createChannel(
        channel: CreateChannelRequest,
        workspaceId: String
    ): Flow<ResponseResult<Channel?>>
}

class ChannelRepositoryImpl @Inject constructor(
    private val channelRemoteSource: ChannelService,
    private val channelLocal: ChannelDao,
    private val spController: SharedPrefsController,
) : ChannelRepository {

    override suspend fun updateChannel(channel: Channel): Flow<ResponseResult<Channel?>> = flow {
        val channelResponse =
            channelRemoteSource.updateChannel(channelId = channel.channelId, channel = channel)

        val state = when (channelResponse.isSuccessful) {
            true -> {
                updateLocal(channelResponse.body())
                spController.updateCacheSettings(CacheClass.Workspace(channel.workspaceId), true)
                ResponseResult.success(channelResponse.body())
            }
            else -> ResponseResult.error(channelResponse.message())
        }
        emit(state)

    }.catch {
        emit(ResponseResult.error("Something went wrong!"))
    }

    override suspend fun deleteChannel(
        channelId: Long,
        workspaceId: String
    ): Flow<ResponseResult<Void?>> = flow {
        val channelResponse =
            channelRemoteSource.deleteChannel(channelId = channelId)

        val state = when (channelResponse.isSuccessful) {
            true -> {
                channelLocal.deleteChannel(channelId)
                spController.updateCacheSettings(CacheClass.Workspace(workspaceId), true)
                ResponseResult.success(channelResponse.body())
            }
            else -> ResponseResult.error(channelResponse.message())
        }
        emit(state)

    }.catch {
        emit(ResponseResult.error("Something went wrong!"))
    }

    override suspend fun createChannel(
        channel: CreateChannelRequest,
        workspaceId: String
    ): Flow<ResponseResult<Channel?>> =
        flow {
            val channelResponse =
                channelRemoteSource.createChannel(channel = channel)

            val state = when (channelResponse.isSuccessful) {
                true -> {
                    channelResponse.body()?.let {
                        channelLocal.insert(it)
                        spController.updateCacheSettings(CacheClass.Workspace(workspaceId), true)
                    }
                    ResponseResult.success(channelResponse.body())
                }
                else -> ResponseResult.error(channelResponse.message())
            }
            emit(state)

        }.catch {
            emit(ResponseResult.error("Something went wrong!"))
        }

    private suspend fun updateLocal(channel: Channel?) {
        channel ?: return
        channelLocal.update(channel)
    }

}