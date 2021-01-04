package app.rootstock.data.user

import androidx.room.Room
import app.rootstock.data.db.FakeDatabase
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    private lateinit var repository: UserRepository

    @Test
    fun testInsertingUser(): Unit = runBlocking {
        val userDao = FakeDatabase().userDao()
        repository = UserRepositoryImpl(userDao)
        val user = User(userId = "213123-231312-3123123-123121123", email = "asda@as.asd")
        repository.insertUser(user)
        val insertedUser = repository.getUser()?.value
    }

}