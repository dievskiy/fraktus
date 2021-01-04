package app.rootstock.api

import app.rootstock.data.version.Version
import retrofit2.Response
import retrofit2.http.GET

interface VersionService {

    @GET("/version/version")
    suspend fun getVersion(): Response<Version>

}