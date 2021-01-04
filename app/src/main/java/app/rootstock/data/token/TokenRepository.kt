package app.rootstock.data.token

import app.rootstock.data.user.UserRepository
import app.rootstock.exceptions.NoUserException
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles token manipulation
 */
interface TokenRepository {
    suspend fun insertToken(token: Token)

    suspend fun getToken(): Token?

    suspend fun getTokenFromNetwork(refreshToken: String): Response<Token>

    suspend fun revokeToken(token: String, accessToken: String)

    suspend fun removeToken()
}


@Singleton
class TokenRepositoryImpl @Inject constructor(
    private val tokenLocalSource: TokenDao,
    private val tokenRemote: TokenService,
    private val userRepository: UserRepository,
) :
    TokenRepository {

    private val userId = runBlocking { userRepository.getUserId() }

    override suspend fun insertToken(token: Token) = tokenLocalSource.deleteAndInsert(token)

    override suspend fun getToken() = tokenLocalSource.getToken()

    override suspend fun getTokenFromNetwork(refreshToken: String): Response<Token> {
        userId ?: throw NoUserException()
        return tokenRemote.refreshToken(
            TokenUpdate(
                refreshToken = refreshToken,
                userId = userId
            )
        )
    }

    override suspend fun revokeToken(token: String, accessToken: String) {
        var id = userId
        if (id == null) {
            id = userRepository.getUserId()
            if (id == null) throw NoUserException()
        }
        tokenRemote.revokeToken(
            tokenRevoke = TokenRevoke(token, id),
            accessToken = "Bearer $accessToken"
        )
    }

    override suspend fun removeToken() {
        tokenLocalSource.deleteAll()
    }


}