package app.rootstock.ui.signup

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rootstock.data.network.CacheCleaner
import app.rootstock.data.network.ResponseResult
import app.rootstock.data.result.Event
import app.rootstock.data.token.Token
import app.rootstock.data.token.TokenRepository
import app.rootstock.data.user.UserRepository
import app.rootstock.data.user.UserWithPassword
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class EventUserSignUp { SUCCESS, USER_EXISTS, INVALID_DATA, FAILED, LOADING }

class SignUpViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val tokenRepository: TokenRepository,
    private val cacheCleaner: CacheCleaner,
) :
    ViewModel() {

    val user = MutableLiveData<SignUpUser>()

    private val _signUpStatus = MutableLiveData<Event<EventUserSignUp>>()
    val signUpStatus: LiveData<Event<EventUserSignUp>> get() = _signUpStatus

    val loading: LiveData<Boolean> = map(signUpStatus) {
        signUpStatus.value?.peekContent() == EventUserSignUp.LOADING
    }

    init {
        user.value = SignUpUser.build()
    }

    fun signUp() {
        // return if loading
        if (_signUpStatus.value?.peekContent() == EventUserSignUp.LOADING) return

        if (user.value?.allValid == false) {
            _signUpStatus.value = Event(EventUserSignUp.INVALID_DATA)
            return
        }

        user.value?.let {
            _signUpStatus.value = Event(EventUserSignUp.LOADING)
            registerUser(it)
        }
    }

    private fun registerUser(user: SignUpUser) {
        viewModelScope.launch {
            when (val userResponse = accountRepository.register(user).first()) {
                is ResponseResult.Success -> {
                    if (userResponse.data != null) {
                        // update local user
                        userRepository.insertUser(userResponse.data)
                        authenticate(user)
                    }
                }
                is ResponseResult.Error -> {
                    _signUpStatus.postValue(Event(EventUserSignUp.USER_EXISTS))
                }
            }
        }
    }

    private suspend fun authenticate(user: UserWithPassword) {
        when (val token = accountRepository.authenticate(user).first()) {
            is ResponseResult.Success -> {
                if (token.data != null) {
                    // update local user
                    tokenRepository.insertToken(
                        Token(
                            accessToken = token.data.accessToken,
                            refreshToken = token.data.refreshToken,
                            tokenType = token.data.tokenType
                        )
                    )
                    // clean all cache on relogin or login
                    cacheCleaner.cleanCache()
                    _signUpStatus.postValue(Event(EventUserSignUp.SUCCESS))
                }
            }
            is ResponseResult.Error -> {
                _signUpStatus.postValue(Event(EventUserSignUp.FAILED))
            }
        }
    }

    fun stopSignUp() {
        viewModelScope.cancel()
    }


}