package app.rootstock.views

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import app.rootstock.data.channel.Channel
import app.rootstock.data.workspace.Workspace
import app.rootstock.databinding.DialogDeleteBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

enum class ItemType {
    CHANNEL, WORKSPACE
}

data class DeleteObj<T> constructor(
    val content: String,
    val id: T,
    val deleteType: ItemType,
    val delete: ((id: T) -> Unit),
    // start and finish positions of text to bold
    val bold: Pair<Int, Int>?
)

/**
 * Dialog Fragment for deleting an entity with @param id and @param name
 * Used for deleting [Channel] and [Workspace]
 */
class DeleteDialogFragment<T>(
    private val deleteObj: DeleteObj<T>
) : AppCompatDialogFragment() {

    private lateinit var binding: DialogDeleteBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogDeleteBinding.inflate(layoutInflater, container, true)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.apply {
            type = deleteObj.deleteType.name.toLowerCase(Locale.ROOT)
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
            message.text = deleteObj.content
            deleteObj.bold?.let {
                try {
                    val spannable = SpannableString(message.text)
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        it.first,
                        it.second,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                    message.text = spannable
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delete.setOnClickListener { deleteObj.delete(deleteObj.id); dismiss() }
                cancel.setOnClickListener { dismiss() }
            }
        }
        if (showsDialog) {
            (requireDialog() as AlertDialog).setView(binding.root)
        }
    }


}
