package app.rootstock.ui.workspace

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rootstock.data.channel.Channel
import app.rootstock.data.network.CreateOperation
import app.rootstock.data.network.ResponseResult
import app.rootstock.data.result.Event
import app.rootstock.data.workspace.CreateWorkspace
import app.rootstock.data.workspace.CreateWorkspaceRequest
import app.rootstock.data.workspace.Workspace
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@FragmentScoped
class WorkspaceCreateViewModel @ViewModelInject constructor(
    private val workspaceRepository: WorkspaceRepository,
) :
    ViewModel() {

    private val _event = MutableLiveData<Event<CreateOperation<Workspace?>>>()
    val event: LiveData<Event<CreateOperation<Workspace?>>> get() = _event

    val workspace = MutableLiveData(CreateWorkspace.build())

    fun setImage(imageUrl: String){
        workspace.value?.imageUrlWorkspace = imageUrl
    }

    fun createWorkspace(wsId: String) {
        val createWorkspace = workspace.value ?: return
        val request = CreateWorkspaceRequest(
            name = createWorkspace.nameWorkspace,
            imageUrl = createWorkspace.imageUrlWorkspace,
            workspaceId = wsId
        )

        viewModelScope.launch {
            when (val response = workspaceRepository.createWorkspace(request).first()) {
                is ResponseResult.Success -> {
                    _event.value = (Event(CreateOperation.Success(response.data)))
                }
                else -> {
                    _event.value = (Event(CreateOperation.Error()))
                }
            }
        }
    }

}