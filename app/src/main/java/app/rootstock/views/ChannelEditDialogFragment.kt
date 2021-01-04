package app.rootstock.views

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import app.rootstock.R
import app.rootstock.adapters.PatternAdapter
import app.rootstock.data.channel.Channel
import app.rootstock.data.channel.ChannelConstants.channelNameRange
import app.rootstock.databinding.DialogChannelEditBinding
import app.rootstock.ui.channels.ColorsViewModel
import app.rootstock.ui.main.ChannelEvent
import app.rootstock.ui.main.WorkspaceViewModel
import app.rootstock.ui.messages.MessageEvent
import app.rootstock.ui.messages.MessagesViewModel
import app.rootstock.utils.InternetUtil
import app.rootstock.utils.autoFitColumns
import app.rootstock.utils.convertDpToPx
import app.rootstock.utils.makeToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


/**
 * Dialog that appears when editing channel.
 */
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChannelEditDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val spanCount = 4
        private const val ARGUMENT_CHANNEL = "ARGUMENT_CHANNEL"
        private const val ARGUMENT_CHANGE = "ARGUMENT_CHANGE"

        fun newInstance(channel: Channel, change: Boolean): ChannelEditDialogFragment {
            return ChannelEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARGUMENT_CHANNEL, channel)
                    putBoolean(ARGUMENT_CHANGE, change)
                }
            }
        }
    }

    private var currentImageUrl: String? = null

    private lateinit var binding: DialogChannelEditBinding

    private val editViewModel: ColorsViewModel by viewModels()

    private var channel: Channel? = null

    private var change: Boolean? = null

    private val adapterToSet =
        PatternAdapter(items = mutableListOf(), ::patternClicked, circle = true)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogChannelEditBinding.inflate(layoutInflater, container, true)
        return binding.root
    }

    private fun patternClicked(position: Int, image: String?) {
        binding.colorsRv.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ChannelPickImageView>(
            R.id.color_item
        )?.togglePicked()

        // if color is picked by user, change line accordingly
        // otherwise, return to the initial color
        changeImage(image)

        // unpick previously picked color
        if (adapterToSet.previousPickedPosition != null && position != adapterToSet.previousPickedPosition) {
            adapterToSet.previousPickedPosition?.let {
                binding.colorsRv.findViewHolderForAdapterPosition(it)?.itemView?.findViewById<ChannelPickImageView>(
                    R.id.color_item
                )?.unPick()
            }
        }
    }

    private fun changeImage(image: String?) {
        currentImageUrl = image
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        channel = arguments?.getSerializable(ARGUMENT_CHANNEL) as? Channel
        change = arguments?.getBoolean(ARGUMENT_CHANGE)
        binding.apply {
            channel = this@ChannelEditDialogFragment.channel
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
        }
        binding.colorsRv.apply {
            adapter = adapterToSet
            autoFitColumns(50, spanCount)
            addItemDecoration(
                GridSpacingItemDecorator(
                    spanCount,
                    requireContext().convertDpToPx(10f).toInt(),
                    false
                )
            )
        }
        editViewModel.images.observe(this) {
            adapterToSet.updateList(it.urls)
        }

        if (showsDialog) {
            (requireDialog() as AlertDialog).setView(binding.root)
        }
        binding.channelEditNameText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.save.isEnabled = p0 != null && p0.length in channelNameRange
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.save.setOnClickListener {
            if (!InternetUtil.isInternetOn()) {
                makeToast(getString(R.string.no_connection))
                dismiss()
                return@setOnClickListener
            }
            channel?.let { channel ->
                val newName =
                    view?.findViewById<EditText>(R.id.channel_edit_name_text)?.text?.toString()
                        ?: return@setOnClickListener
                // if image has not been changed - use initial image
                if (currentImageUrl == null) currentImageUrl = channel.imageUrl
                val newChannel = channel.copy(name = newName, imageUrl = currentImageUrl)
                if (change == true) {
                    val messagesViewModel: MessagesViewModel by activityViewModels()
                    messagesViewModel.setChannel(newChannel)
                    messagesViewModel.modifyChannel()
                    messagesViewModel.updateChannel(newChannel)
                } else {
                    val main: WorkspaceViewModel by activityViewModels()
                    main.updateChannel(newChannel)
                }
                dismiss()
            }
        }

        binding.cancel.setOnClickListener { dismiss() }
    }

}
