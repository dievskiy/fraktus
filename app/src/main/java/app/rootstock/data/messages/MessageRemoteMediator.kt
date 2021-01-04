package app.rootstock.data.messages


import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import app.rootstock.api.MessageService
import app.rootstock.data.db.AppDatabase
import app.rootstock.data.db.RemoteKeys
import app.rootstock.data.db.RemoteKeysDao
import app.rootstock.data.prefs.CacheClass
import app.rootstock.data.prefs.SharedPrefsController
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException

private const val STARTING_PAGE_INDEX = 0
private const val MESSAGES_OFFSET = 100

@OptIn(ExperimentalPagingApi::class)
class MessageRemoteMediator(
    private val channelId: Long,
    private val service: MessageService,
    private val remoteKeysDao: RemoteKeysDao,
    private val messageDao: MessageDao,
    private val database: AppDatabase,
    private val spController: SharedPrefsController,
) : RemoteMediator<Int, Message>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Message>
    ): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(MESSAGES_OFFSET) ?: STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                if (remoteKeys == null) {
                    // The LoadType is PREPEND so some data was loaded before,
                    // so we should have been able to get remote keys
                    // If the remoteKeys are null, then we're an invalid state and we have a bug
                    throw InvalidObjectException("Remote key and the prevKey should not be null")
                }
                // If the previous key is null, then we can't request more data
                remoteKeys.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = true
                )
                remoteKeys.prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                if (remoteKeys == null || remoteKeys.nextKey == null) {
                    throw InvalidObjectException("Remote key should not be null for $loadType")
                }
                remoteKeys.nextKey
            }
        }

        try {
            val cacheControl =
                if (spController.shouldUpdateCache(CacheClass.Channel(channelId))) "no-cache" else null
            val apiResponse =
                service.getMessages(
                    channelId = channelId,
                    offset = page,
                    cacheControl = cacheControl
                )
            val messages = apiResponse.map {
                it.channelId = channelId
                it
            }
            val endOfPaginationReached = messages.isEmpty()
            database.withTransaction {
                val prevKey = if (page == STARTING_PAGE_INDEX) null else page - MESSAGES_OFFSET
                val nextKey = if (endOfPaginationReached) null else page + MESSAGES_OFFSET
                val keys = messages.map {
                    RemoteKeys(
                        messageId = it.messageId,
                        prevKey = prevKey,
                        nextKey = nextKey,
                        channelId = channelId
                    )
                }
                remoteKeysDao.upsertAll(keys)
                // todo upsert
                messageDao.insertAll(messages)
            }
            spController.updateCacheSettings(CacheClass.Channel(channelId), false)
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Message>): RemoteKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { message ->
                // Get the remote keys of the last item retrieved
                remoteKeysDao.remoteKeysMessageId(message.messageId, channelId)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Message>): RemoteKeys? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { message ->
                // Get the remote keys of the first items retrieved
                remoteKeysDao.remoteKeysMessageId(message.messageId, channelId)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Message>
    ): RemoteKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.messageId?.let { id ->
                remoteKeysDao.remoteKeysMessageId(id, channelId)
            }
        }
    }

}