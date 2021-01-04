package app.rootstock.api

import app.rootstock.data.channel.ImageUrls
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ColorService {

    @GET("/images/links")
    suspend fun getColors(@Header("Cache-Control") cacheControl: String? = "no-cache"): Response<ImageUrls>
}