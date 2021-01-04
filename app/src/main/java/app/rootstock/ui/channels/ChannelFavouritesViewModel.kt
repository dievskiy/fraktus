package app.rootstock.ui.channels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rootstock.data.channel.Channel
import app.rootstock.data.result.Event
import app.rootstock.ui.channels.favourites.ChannelFavouriteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

enum class FavouritesEvent {
    MAXIMUM_REACHED
}

class ChannelFavouritesViewModel @ViewModelInject constructor(
    private val channelRepository: ChannelFavouriteRepository,
) : ViewModel() {


    private val _favourites = MutableLiveData<List<Channel>>()
    val favourites: LiveData<List<Channel>> get() = _favourites

    private val _event = MutableLiveData<Event<FavouritesEvent>>()
    val event: LiveData<Event<FavouritesEvent>> get() = _event

    private var job: Job? = null

    init {
        job = viewModelScope.launch {
            channelRepository.getFavourites().collect {
                _favourites.value = it
            }
        }
    }

    fun toggle(channelId: Long) = viewModelScope.launch {
        try {
            channelRepository.toggleFavourite(channelId)
        } catch (e: IllegalStateException) {
            _event.value = Event(FavouritesEvent.MAXIMUM_REACHED)
        }
    }

    suspend fun isFavourite(channelId: Long) = channelRepository.isFavourite(channelId)
}
