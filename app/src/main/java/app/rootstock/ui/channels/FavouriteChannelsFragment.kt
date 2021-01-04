package app.rootstock.ui.channels

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import app.rootstock.R
import app.rootstock.adapters.ChannelFavouritesAdapter
import app.rootstock.data.channel.Channel
import app.rootstock.databinding.FragmentFavouritesBinding
import app.rootstock.ui.main.WorkspaceActivity
import app.rootstock.ui.main.WorkspaceViewModel
import app.rootstock.ui.messages.MessageEvent
import app.rootstock.ui.messages.MessagesFragment
import app.rootstock.ui.messages.MessagesViewModel
import app.rootstock.utils.autoFitColumns
import app.rootstock.utils.convertDpToPx
import app.rootstock.utils.makeToast
import app.rootstock.views.GridSpacingItemDecoratorWithCustomCenter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*


@AndroidEntryPoint
class FavouriteChannelsFragment : Fragment() {

    private val viewModel: ChannelFavouritesViewModel by viewModels()

    private val main: WorkspaceViewModel by activityViewModels()

    private lateinit var binding: FragmentFavouritesBinding

    lateinit var adapter: ChannelFavouritesAdapter

    // current size of favourite channels in view
    private var currentSize: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }


    private fun showSendSharedTextDialog(message: String, channel: Channel) {
        val dialog = MaterialAlertDialogBuilder(requireContext()).create()
        val body = getString(R.string.channel_message_send, channel.name)
        val view = layoutInflater.inflate(R.layout.dialog_send_channel, null)
        dialog.setView(view)
        view.findViewById<TextView>(R.id.message)?.let {
            try {
                val spannable = SpannableString(body)
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    36,
                    body.length - 1,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
                it.text = spannable
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        view.findViewById<View>(R.id.cancel)?.setOnClickListener {
            dialog.dismiss()
        }
        view.findViewById<View>(R.id.send)?.setOnClickListener {
            var messageSend = message
            if (message.length > MessagesFragment.MAX_MESSAGE_LENGTH) messageSend =
                message.slice(0 until MessagesFragment.MAX_MESSAGE_LENGTH)
            dialog.dismiss()
            val messageViewModel: MessagesViewModel by viewModels()
            messageViewModel.setChannel(channel)
            messageViewModel.sendMessage(messageSend)
            messageViewModel.messageEvent.observe(this) {
                when (val m = it?.getContentIfNotHandled()) {
                    is MessageEvent.Error -> {
                        if (m.message.toLowerCase().contains("unprocessable"))
                            makeToast(getString(R.string.too_long_message))
                        else makeToast(getString(R.string.error_message_not_send))
                    }
                    is MessageEvent.Created -> {
                        requireActivity().finish()
                    }
                    is MessageEvent.NoConnection -> {
                        makeToast(getString(R.string.no_connection))
                    }
                    else -> {
                    }
                }
            }

        }
        dialog.show()
    }


    @ExperimentalCoroutinesApi
    private fun openChannel(channel: Channel) {
        requireActivity().intent.getStringExtra(Intent.EXTRA_TEXT)?.let { message ->
            showSendSharedTextDialog(message, channel)
            return
        }
        val intent = Intent(requireActivity(), ChannelActivity::class.java)
        intent.putExtra(WorkspaceActivity.BUNDLE_CHANNEL_EXTRA, channel)
        requireActivity().startActivityForResult(
            intent,
            WorkspaceActivity.REQUEST_CODE_CHANNEL_ACTIVITY
        )
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChannelFavouritesAdapter(viewLifecycleOwner, ::openChannel)
        binding.recyclerView.apply {
            adapter = this@FavouriteChannelsFragment.adapter
            autoFitColumns(
                CHANNELS_COLUMN_WIDTH_DP,
                CHANNELS_SPAN_COUNT
            )
            // inner and bottom-element padding are same
            addItemDecoration(
                GridSpacingItemDecoratorWithCustomCenter(
                    spanCount = CHANNELS_SPAN_COUNT,
                    spacing = requireContext().convertDpToPx(5f).toInt(),
                    centerSpacing = requireContext().convertDpToPx(5f).toInt(),
                    bottomSpacing = requireContext().convertDpToPx(10f).toInt()
                )
            )
            val pad = requireContext().convertDpToPx(20f).toInt()
            setPadding(
                pad,
                paddingTop,
                pad,
                pad
            )
        }

        viewModel.favourites.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            if (currentSize != null && currentSize != it.size)
                main.toggleBackdrop(close = true)
            currentSize = it.size
            if (it.isNullOrEmpty()) {
                binding.noChannels.isVisible = true
            } else {
                if (binding.noChannels.isVisible) binding.noChannels.isVisible = false
            }
            if (main.favouriteShowed.value == false && !it.isNullOrEmpty()) {
                lifecycleScope.launch {
                    delay(300)
                    main.toggleBackdrop(close = false)
                }
            }
            if (it != null) main.showFavourite()
        }

    }

    companion object {
        const val CHANNELS_SPAN_COUNT = 2
        const val CHANNELS_COLUMN_WIDTH_DP = 100

        fun newInstance(): FavouriteChannelsFragment {
            return FavouriteChannelsFragment()
        }
    }

}
