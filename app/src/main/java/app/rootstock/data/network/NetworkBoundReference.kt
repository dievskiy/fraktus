package app.rootstock.data.network

import androidx.annotation.WorkerThread
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map



sealed class ResponseResult<T> {
    data class Success<T>(val data: T) : ResponseResult<T>()
    data class Error<T>(val message: String) : ResponseResult<T>()

    companion object {
        fun <T> success(data: T) = Success(data)
        fun <T> error(message: String) = Error<T>(message)
    }
}

/**
 * [RESULT] type for db.
 * [REQUEST] type for network.
 */
@ExperimentalCoroutinesApi
abstract class NetworkBoundRepository<RESULT, REQUEST> {

    suspend fun asFlow() = flow<ResponseResult<RESULT>> {

        emit(ResponseResult.success(fetchFromLocal().first()))

        val response = fetchFromRemote()

        if (response != null) {
            persistData(response)
        } else {
            emit(ResponseResult.error("response is null"))
        }

        emitAll(fetchFromLocal().map {
            ResponseResult.success(it)
        })
    }.catch { e ->
        emit(ResponseResult.error("Something went wrong!"))
        e.printStackTrace()
    }

    /**
     * Saves the data to the persistence storage.
     */
    @WorkerThread
    protected abstract suspend fun persistData(response: REQUEST)

    /**
     * Returns the data from persistence storage.
     */
    @WorkerThread
    protected abstract suspend fun fetchFromLocal(): Flow<RESULT>

    /**
     * Received data from network.
     */
    @WorkerThread
    protected abstract suspend fun fetchFromRemote(): REQUEST?
}
