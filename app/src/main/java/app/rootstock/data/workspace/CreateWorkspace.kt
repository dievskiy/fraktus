package app.rootstock.data.workspace

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import app.rootstock.BR
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class CreateWorkspaceRequest(
    private val name: String,
    @SerializedName("background_color")
    private val color: String? = null,
    @SerializedName("image_url")
    private val imageUrl: String?,
    @SerializedName("ws_id")
    val workspaceId: String?,
)

class CreateWorkspace
    : BaseObservable(), Serializable {

    companion object {
        fun build(): CreateWorkspace {
            return CreateWorkspace()
        }
    }

    @Bindable
    var nameValidWorkspace: Boolean = false
        get() = field


    @Bindable
    var nameWorkspace: String = String()
        set(value) {
            field = value
            checkName()
            notifyPropertyChanged(BR.nameWorkspace)
        }
        get() = field

    var imageUrlWorkspace: String = ""


    private fun checkName() {
        nameValidWorkspace = isNameValid()
        notifyPropertyChanged(BR.nameValidWorkspace)
    }

    private fun isNameValid() = nameWorkspace.length in WorkspaceConstants.workspaceNameRange
}