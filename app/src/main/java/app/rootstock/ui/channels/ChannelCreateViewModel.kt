package app.rootstock.ui.channels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rootstock.data.channel.Channel
import app.rootstock.data.channel.CreateChannel
import app.rootstock.data.channel.CreateChannelRequest
import app.rootstock.data.network.CreateOperation
import app.rootstock.data.network.ResponseResult
import app.rootstock.data.result.Event
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@FragmentScoped
class ChannelCreateViewModel @ViewModelInject constructor(
    private val channelRepository: ChannelRepository,
) :
    ViewModel() {

    private val _eventChannel = MutableLiveData<Event<CreateOperation<Channel?>>>()
    val eventChannel: LiveData<Event<CreateOperation<Channel?>>> get() = _eventChannel

    val channel = MutableLiveData(CreateChannel())

    fun createChannel(workspaceId: String) {
        val channel = channel.value ?: return
        val newChannel = CreateChannelRequest(
            name = channel.name,
            workspaceId = workspaceId,
            imageUrl = channel.imageUrl
        )
        viewModelScope.launch {
            when (val channelResp =
                channelRepository.createChannel(channel = newChannel, workspaceId).first()) {
                is ResponseResult.Success -> {
                    _eventChannel.postValue(Event(CreateOperation.Success(channelResp.data)))
                }
                is ResponseResult.Error -> {
                    _eventChannel.postValue(Event(CreateOperation.Error()))
                }
            }
        }
    }

}
