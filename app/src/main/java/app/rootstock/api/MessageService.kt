package app.rootstock.api

import app.rootstock.data.messages.Message
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface MessageService {

    @GET("/messages/")
    suspend fun getMessages(
        @Query("channel_id") channelId: Long,
        @Query("offset") offset: Int = 0,
        @Header("Cache-Control") cacheControl: String? = null,
    ): List<Message>

    @POST("/messages/")
    suspend fun sendMessages(
        @Body sendMessage: SendMessage,
    ): Response<Message>

    @DELETE("/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: Long,
    ): Response<Void>

    @PATCH("/messages/{messageId}")
    suspend fun editMessage(
        @Body editMessage: EditMessage,
        @Path("messageId") messageId: Long,
    ): Response<Message>
}

data class SendMessage(
    val content: String,
    @SerializedName("channel_id") val channelId: Long
)

data class EditMessage(
    val content: String
)