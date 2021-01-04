package app.rootstock.data.user

import androidx.lifecycle.LiveData
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    fun getUser(): LiveData<User?>
    suspend fun getUserId(): String?
    suspend fun insertUser(user: User)
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getUser() = userDao.searchUser()

    override suspend fun getUserId() = userDao.getUserId()

    override suspend fun insertUser(user: User) = userDao.deleteAndInsert(user)

}