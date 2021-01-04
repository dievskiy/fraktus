package app.rootstock.data.network

import app.rootstock.data.token.TokenRepository
import app.rootstock.exceptions.NoUserException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authenticator that handles 401 Unauthorized error.
 * Refreshes access token using refresh token or emits an relogin event
 * through [ReLogInObservable] when refresh token is also expired.
 */
@Singleton
class ServerAuthenticator @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val tokenInterceptor: TokenInterceptor,
    private val reLogInObservable: ReLogInObservable,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // prevent infinite loops
        if (!response.request().header("Authorization")
                .equals("Bearer " + tokenInterceptor.currentToken)
        ) {
            return null
        }

        // get refresh token from db
        val refreshToken = runBlocking {
            tokenRepository.getToken()?.refreshToken
        }
        if (refreshToken == null) {
            relogin()
            return null
        }

        // refresh token
        val newTokenResponse = try {
            runBlocking {
                tokenRepository.getTokenFromNetwork(refreshToken)
            }
        } catch (e: NoUserException) {
            relogin()
            return null
        }

        val newToken = newTokenResponse.body()

        // refresh token has expired
        if (newTokenResponse.code() == 401 || newToken == null) {
            relogin()
            return null
        }

        tokenInterceptor.currentToken = newToken.accessToken

        // update token in db
        runBlocking {
            tokenRepository.insertToken(newToken)
        }

        // revoke old token
        CoroutineScope(Dispatchers.Main).launch {
            tokenRepository.revokeToken(refreshToken, newToken.accessToken)
        }
        return response.request().newBuilder()
            .header("Authorization", "Bearer ${newToken.accessToken}").build()
    }

    private fun relogin() {
        // null token so in case of relogin we'll get a fresh one from db
        tokenInterceptor.nullToken()
        reLogInObservable.notifyObservers()
    }

    fun nullToken(){
        tokenInterceptor.nullToken()
    }

}