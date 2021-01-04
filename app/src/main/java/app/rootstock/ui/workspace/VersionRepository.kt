package app.rootstock.ui.workspace

import app.rootstock.api.VersionService
import app.rootstock.data.network.ResponseResult
import app.rootstock.data.version.Version
import app.rootstock.data.version.VersionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface VersionRepository {
    suspend fun getVersionRemote(): Flow<ResponseResult<Version?>>
    suspend fun getVersionLocal(): Version?
}

class VersionRepositoryImpl @Inject constructor(
    private val versionDao: VersionDao,
    private val versionRemote: VersionService
) : VersionRepository {

    private var version: Version? = null

    override suspend fun getVersionRemote(): Flow<ResponseResult<Version?>> = flow {
        val response = versionRemote.getVersion()

        val state = when (response.isSuccessful) {
            true -> {
                ResponseResult.success(response.body())
            }
            else -> ResponseResult.error(response.message())
        }
        if (response.isSuccessful) {
            // insert new only if there is no version already
            if (getVersionLocal() == null)
                versionDao.deleteAndInsert(response.body())
        }
        emit(state)

    }.catch {
        emit(ResponseResult.error("Unable to get version"))
    }

    override suspend fun getVersionLocal(): Version? {
        if (version == null) {
            version = versionDao.get()
        }
        return version
    }


}