package app.rootstock.api

import app.rootstock.data.channel.Channel
import app.rootstock.data.channel.CreateChannelRequest
import retrofit2.Response
import retrofit2.http.*

interface ChannelService {

    @PATCH("/channels/{channelId}")
    suspend fun updateChannel(
        @Path("channelId") channelId: Long,
        @Body channel: Channel,
    ): Response<Channel>


    @DELETE("/channels/{channelId}")
    suspend fun deleteChannel(
        @Path("channelId") channelId: Long
    ): Response<Void>


    @POST("/channels/")
    suspend fun createChannel(
        @Body channel: CreateChannelRequest,
    ): Response<Channel>
}