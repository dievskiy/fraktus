package app.rootstock.ui.channels.favourites

import app.rootstock.data.channel.Channel
import app.rootstock.data.channel.ChannelConstants.maxFavouriteChannels
import app.rootstock.data.channel.ChannelFavouriteDao
import kotlinx.coroutines.flow.Flow
import java.lang.IllegalStateException
import javax.inject.Inject


interface ChannelFavouriteRepository {
    suspend fun getFavourites(): Flow<List<Channel>>
    suspend fun toggleFavourite(channelId: Long)
    suspend fun isFavourite(channelId: Long): Boolean
}

class ChannelFavouriteRepositoryImpl @Inject constructor(
    private val favourites: ChannelFavouriteDao,
) : ChannelFavouriteRepository {
    override suspend fun getFavourites(): Flow<List<Channel>> = favourites.get()

    override suspend fun toggleFavourite(channelId: Long) {
        if (isFavourite(channelId)) favourites.remove(channelId)
        else {
            // allow only 4 favourite channels
            if (favourites.getSize() > maxFavouriteChannels - 1) throw IllegalStateException()
            else favourites.add(channelId)
        }
    }

    override suspend fun isFavourite(channelId: Long): Boolean {
        val fav = favourites.getFavourite(channelId)
        if (fav != null) return true
        return false
    }


}