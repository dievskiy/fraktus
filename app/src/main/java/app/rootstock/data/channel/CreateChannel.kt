package app.rootstock.data.channel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import app.rootstock.BR
import app.rootstock.data.channel.ChannelConstants.channelNameRange
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreateChannelRequest(
    private val name: String,
    @SerializedName("background_color")
    private val color: String? = null,
    @SerializedName("image_url")
    private val imageUrl: String?,
    @SerializedName("ws_id")
    private val workspaceId: String?,
)

class CreateChannel
    : BaseObservable(), Serializable {

    companion object {
        fun build(): CreateChannel {
            return CreateChannel()
        }
    }

    @Bindable
    var nameValid: Boolean = false
        get() = field


    @Bindable
    var name: String = String()
        set(value) {
            field = value
            checkName()
            notifyPropertyChanged(BR.name)
        }
        get() = field

    var imageUrl: String = ""


    private fun checkName() {
        nameValid = isNameValid()
        notifyPropertyChanged(BR.nameValid)
    }


    private fun isNameValid() = name.length in channelNameRange
}