package app.rootstock.ui.channels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import app.rootstock.data.channel.PatternsDelegate
import app.rootstock.data.channel.ImageUrls
import app.rootstock.data.network.ResponseResult
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@FragmentScoped
class ColorsViewModel @ViewModelInject constructor(
    private val patternDelegate: PatternsDelegate,
) :
    ViewModel() {

    private val _images = MutableLiveData<ImageUrls>()
    val images: LiveData<ImageUrls> get() = _images

    init {
        viewModelScope.launch {
            when (val response = patternDelegate.getPatterns().first()) {
                is ResponseResult.Success -> {
                    _images.value = response.data
                }
                is ResponseResult.Error -> {
                }
            }
        }
    }


}