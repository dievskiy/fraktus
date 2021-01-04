package app.rootstock.ui.settings

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import app.rootstock.data.db.AppDatabase
import app.rootstock.data.network.CacheCleaner
import app.rootstock.data.network.ResponseResult
import app.rootstock.data.network.ServerAuthenticator
import app.rootstock.data.result.Event
import app.rootstock.data.token.TokenRepository
import app.rootstock.data.user.UserRepository
import app.rootstock.ui.signup.AccountRepository
import app.rootstock.ui.workspace.WorkspaceRepository
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ActivityScoped
class SettingsViewModel @ViewModelInject constructor(
    private val tokenRepository: TokenRepository,
    private val accountRepository: AccountRepository,
    private val database: AppDatabase,
    private val cacheCleaner: CacheCleaner,
    private val serverAuthenticator: ServerAuthenticator,
) :
    ViewModel() {

    private val _event = MutableLiveData<Event<SettingsEvent>>()
    val event: LiveData<Event<SettingsEvent>> get() = _event

    fun logOut() {
        // when log out, revoke token and clear user login data
        viewModelScope.launch {
            userData.value?.userId?.let {
                val token = tokenRepository.getToken() ?: return@launch
                database.withTransaction {
                    database.workspaceDao().deleteAll()
                    tokenRepository.revokeToken(
                        token = token.refreshToken,
                        accessToken = token.accessToken
                    )
                    tokenRepository.removeToken()
                    database.userDao().deleteAll()
                    cacheCleaner.cleanCache()
                    serverAuthenticator.nullToken()
                    _event.postValue(Event(SettingsEvent.LOG_OUT))
                }
            }
        }
    }

    fun deleteAccount(email: String) {
        viewModelScope.launch {
            userData.value?.userId?.let {
                accountRepository.delete(email).collect {
                    when (it) {
                        is ResponseResult.Success -> {
                            database.withTransaction {
                                database.workspaceDao().deleteAll()
                                tokenRepository.removeToken()
                                database.userDao().deleteAll()
                                cacheCleaner.cleanCache()
                                serverAuthenticator.nullToken()
                                _event.postValue(Event(SettingsEvent.DELETED))
                            }
                        }
                        is ResponseResult.Error -> {
                            _event.postValue(Event(SettingsEvent.FAILED))
                        }
                    }
                }
            }
        }
    }

    val userData = database.userDao().searchUser()

}

enum class SettingsEvent {
    LOG_OUT, DELETED, FAILED
}