package app.rootstock.data.token

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface TokenService {
    @POST("/refresh-token")
    suspend fun refreshToken(
        @Body tokenUpdate: TokenUpdate
    ): Response<Token>

    /**
     * Should return 204 No Content
     */
    @POST("/revoke-refresh")
    suspend fun revokeToken(
        @Header("Authorization") accessToken: String,
        @Body tokenRevoke: TokenRevoke,
    ): Response<Void>
}

