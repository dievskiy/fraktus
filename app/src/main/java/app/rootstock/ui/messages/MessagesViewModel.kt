package app.rootstock.ui.messages

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.rootstock.api.EditMessage
import app.rootstock.api.SendMessage
import app.rootstock.data.channel.Channel
import app.rootstock.data.messages.Message
import app.rootstock.data.messages.MessageRepository
import app.rootstock.data.network.ResponseResult
import app.rootstock.data.result.Event
import app.rootstock.ui.channels.ChannelRepository
import app.rootstock.ui.main.ChannelEvent
import app.rootstock.utils.InternetUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MessageEvent() {
    class Error(val message: String) : MessageEvent()
    object Created : MessageEvent()
    object Deleted : MessageEvent()
    object CancelEditing : MessageEvent()
    object UpdateFailed : MessageEvent()
    class NoConnection(val attempt: String? = null) : MessageEvent()
}

@OptIn(ExperimentalPagingApi::class)
class MessagesViewModel @ViewModelInject constructor(
    private val repository: MessageRepository,
    private val channelRepository: ChannelRepository,
) :
    ViewModel() {

    private val _messageEvent = MutableLiveData<Event<MessageEvent>>()
    val messageEvent: LiveData<Event<MessageEvent>> get() = _messageEvent

    private val _isEditing = MutableLiveData(false)
    val isEditing: LiveData<Boolean> get() = _isEditing

    private val _channel = MutableLiveData<Channel>()
    val channel: LiveData<Channel>
        get() = _channel

    fun setChannel(channel: Channel) {
        _channel.value = channel
    }

    private var currentQueryValue: Long? = null

    private var currentSearchResult: Flow<PagingData<Message>>? = null

    private val _modifiedChannel = MutableLiveData(false)
    val modifiedChannel: LiveData<Boolean> get() = _modifiedChannel

    fun searchRepo(channelId: Long, refresh: Boolean = false): Flow<PagingData<Message>> {
        val lastResult = currentSearchResult
        if (channelId == currentQueryValue && lastResult != null && !refresh) {
            return lastResult
        }
        currentQueryValue = channelId
        val newResult: Flow<PagingData<Message>> = repository.getSearchResultStream(channelId)
            .cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }

    private fun isConnected(content: String? = null): Boolean {
        if (!InternetUtil.isInternetOn()) {
            _messageEvent.value = Event(MessageEvent.NoConnection(content))
            return false
        }
        return true
    }

    fun sendMessage(content: String?) {
        content ?: return
        val id = _channel.value?.channelId ?: return
        if (!isConnected(content)) return

        val sendMessage = SendMessage(content = content, channelId = id)
        sendMessage(sendMessage)
    }

    private fun sendMessage(sendMessage: SendMessage) {
        viewModelScope.launch {
            val wsId = channel.value?.workspaceId ?: return@launch
            when (val message =
                repository.sendMessage(message = sendMessage, workspaceId = wsId).first()) {
                is ResponseResult.Success -> {
                    _messageEvent.value = Event(MessageEvent.Created)
                    modifyChannel()
                }
                is ResponseResult.Error -> {
                    _messageEvent.postValue(Event(MessageEvent.Error(message = message.message)))
                }
            }
        }
    }

    fun editMessage(id: Long, content: String) {
        if (!isConnected()) return
        val editMessage = EditMessage(content = content)
        viewModelScope.launch {
            val wsId = channel.value?.workspaceId ?: return@launch
            val channelId = channel.value?.channelId ?: return@launch
            when (val response = repository.editMessage(editMessage, id, wsId, channelId).first()) {
                is ResponseResult.Success -> {
                    _messageEvent.value = Event(MessageEvent.Created)
                    modifyChannel()
                }
                is ResponseResult.Error -> {
                    _messageEvent.postValue(Event(MessageEvent.Error(message = response.message)))
                }
            }
        }
    }

    fun modifyChannel() {
        _modifiedChannel.value = true
    }

    fun startEditing() {
        _isEditing.value = true
    }

    fun stopEditing() {
        _isEditing.value = false
    }

    fun cancelEdit() {
        _messageEvent.value = Event(MessageEvent.CancelEditing)
    }

    fun deleteMessage(messageId: Long) {
        if (!isConnected()) return
        viewModelScope.launch {
            val wsId = channel.value?.workspaceId ?: return@launch
            val channelId = channel.value?.channelId ?: return@launch

            when (val response = repository.deleteMessage(messageId, wsId, channelId).first()) {
                is ResponseResult.Success -> {
                    _messageEvent.value = Event(MessageEvent.Deleted)
                    modifyChannel()
                }
                is ResponseResult.Error -> {
                    _messageEvent.postValue(Event(MessageEvent.Error(message = response.message)))
                }
            }
        }
    }

    fun updateChannel(channel: Channel) {
        if (channel.isValid()) {
            viewModelScope.launch {
                when (val c = channelRepository.updateChannel(channel).first()) {
                    is ResponseResult.Success -> {
                    }
                    is ResponseResult.Error -> {
                        _messageEvent.value = Event(MessageEvent.UpdateFailed)
                    }
                }
            }
        } else {
            _messageEvent.value = Event(MessageEvent.UpdateFailed)
        }
    }

}