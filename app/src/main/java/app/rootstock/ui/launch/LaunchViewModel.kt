package app.rootstock.ui.launch

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rootstock.data.result.Event
import app.rootstock.data.user.UserRepository
import app.rootstock.ui.workspace.WorkspaceRepository
import kotlinx.coroutines.launch

class LaunchViewModel @ViewModelInject constructor(
    repository: UserRepository,
) :
    ViewModel() {

    val user = repository.getUser()

    val launchDestination = map(user) {
        if (it == null) {
            Event(Launch.SIGN_UP_ACTIVITY)
        }  else {
            Event(Launch.WORKSPACE_ACTIVITY)
        }
    }
}

enum class Launch {
    WORKSPACE_ACTIVITY, SIGN_UP_ACTIVITY
}