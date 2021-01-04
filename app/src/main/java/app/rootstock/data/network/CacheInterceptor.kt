package app.rootstock.data.network

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


class CacheInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(60 * 24, TimeUnit.MINUTES)
            .build()

        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", cacheControl.toString())
            .build()
    }
}


@Singleton
class CacheCleaner @Inject constructor(private val okHttpClient: OkHttpClient) {

    fun cleanCache() {
        okHttpClient.cache()?.evictAll()
    }
}