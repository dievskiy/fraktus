package app.rootstock.ui.signup

import app.rootstock.api.UserDeleteService
import app.rootstock.api.UserServices
import app.rootstock.api.UserSignUpService
import app.rootstock.data.network.ResponseResult
import app.rootstock.data.token.Token
import app.rootstock.data.user.DeleteUser
import app.rootstock.data.user.User
import app.rootstock.data.user.UserWithPassword
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.nio.file.attribute.UserDefinedFileAttributeView
import javax.inject.Inject

interface AccountRepository {
    suspend fun register(user: SignUpUser): Flow<ResponseResult<User?>>
    suspend fun authenticate(user: UserWithPassword): Flow<ResponseResult<Token?>>
    suspend fun getUserRemote(token: String): Flow<ResponseResult<User?>>
    suspend fun delete(email: String): Flow<ResponseResult<Void?>>
}

/**
 * Repository for user account manipulation
 */
class AccountRepositoryImpl @Inject constructor(
    private val signUpService: UserSignUpService,
    private val services: UserServices,
    private val userInfoService: UserServices,
    private val userDelete: UserDeleteService,
) : AccountRepository {

    override suspend fun register(user: SignUpUser): Flow<ResponseResult<User?>> = flow {
        val tokenResponse = signUpService.createUser(user)

        val state = when (tokenResponse.isSuccessful) {
            true -> ResponseResult.success(tokenResponse.body())
            else -> ResponseResult.error(tokenResponse.message())
        }
        emit(state)
    }.catch {
        emit(ResponseResult.error("Something went wrong!"))
    }


    override suspend fun authenticate(user: UserWithPassword): Flow<ResponseResult<Token?>> = flow {
        val tokenResponse = services.logIn(username = user.email, password = user.password)

        val state = when (tokenResponse.isSuccessful) {
            true -> ResponseResult.success(tokenResponse.body())
            else -> ResponseResult.error(tokenResponse.message())
        }
        emit(state)
    }.catch {
        emit(ResponseResult.error("Something went wrong!"))
    }

    /**
     * requests user_id and email after login in case of email update
     */
    override suspend fun getUserRemote(token: String): Flow<ResponseResult<User?>> = flow {
        val userResponse = userInfoService.getUser("Bearer $token")

        val state = when (userResponse.isSuccessful) {
            true -> ResponseResult.success(userResponse.body())
            else -> ResponseResult.error(userResponse.message())
        }

        emit(state)
    }.catch {
        emit(ResponseResult.error("Something went wrong!"))
    }

    override suspend fun delete(email: String): Flow<ResponseResult<Void?>> = flow {
        val userResponse = userDelete.delete(DeleteUser(email))

        val state = when (userResponse.isSuccessful) {
            true -> ResponseResult.success(userResponse.body())
            else -> ResponseResult.error(userResponse.message())
        }

        emit(state)
    }.catch {
        emit(ResponseResult.error("Something went wrong!"))
    }

}