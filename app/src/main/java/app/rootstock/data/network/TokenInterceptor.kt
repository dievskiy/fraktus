package app.rootstock.data.network

import app.rootstock.data.token.TokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Interceptor that adds an Authorization header with valid jwt access token
 */
class TokenInterceptor @Inject constructor(
    private val tokenRepository: TokenRepository,
) :
    Interceptor {

    var currentToken: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {

        if (currentToken == null) {
            currentToken = runBlocking {
                tokenRepository.getToken()?.accessToken
            }

        }

        currentToken?.let {
            val request = createRequestWithAccessToken(chain.request(), it)
            return chain.proceed(request)
        } ?: return chain.proceed(chain.request())
    }

    private fun createRequestWithAccessToken(request: Request, token: String): Request =
        request.newBuilder().header("Authorization", "Bearer $token").build()

    fun nullToken() {
        currentToken = null
    }

}

