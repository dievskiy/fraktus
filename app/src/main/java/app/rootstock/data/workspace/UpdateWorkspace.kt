package app.rootstock.data.workspace

import com.google.gson.annotations.SerializedName

data class UpdateWorkspaceRequest(
    private val name: String,
    @SerializedName("image_url")
    private val imageUrl: String?,
)

