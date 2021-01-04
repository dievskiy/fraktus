package app.rootstock.ui.messages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearSmoothScroller
import app.rootstock.R
import app.rootstock.adapters.MessageAdapter
import app.rootstock.data.messages.Message
import app.rootstock.data.messages.MessageRepository.Companion.NETWORK_PAGE_SIZE
import app.rootstock.databinding.MessagesFragmentBinding
import app.rootstock.utils.convertDpToPx
import app.rootstock.utils.hideSoftKeyboard
import app.rootstock.utils.makeToast
import app.rootstock.utils.showKeyboard
import app.rootstock.views.MessagesLoadStateAdapter
import app.rootstock.views.SpacingItemDecorationReversed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_account_start.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
@OptIn(ExperimentalPagingApi::class)
class MessagesFragment : Fragment() {

    private val viewModel: MessagesViewModel by activityViewModels()

    private lateinit var binding: MessagesFragmentBinding

    private var searchJob: Job? = null

    private lateinit var adapter: MessageAdapter

    private var created = false

    private var messageEditingId: Long? = null

    private var messageBeforeEdit: String? = null

    private var unSelect: (() -> Unit)? = null

    private fun search(channelId: Long, refresh: Boolean = false) {
        // Make sure we cancel the previous job before creating a new one
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.searchRepo(channelId, refresh).collectLatest {
                try {
                    adapter.submitData(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MessagesFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        viewModel.channel.observe(viewLifecycleOwner) {
            it ?: return@observe
            search(channelId = it.channelId)
            initSearch()
        }

        val itemDecorator =
            SpacingItemDecorationReversed(requireContext().convertDpToPx(SPACING).toInt())
        binding.list.apply {
            addItemDecoration(itemDecorator)
        }

        binding.send.setOnClickListener {
            sendMessage()
        }
        binding.retryButton.setOnClickListener { if (::adapter.isInitialized) adapter.retry() }
        viewModel.messageEvent.observe(viewLifecycleOwner) { event ->
            when (val m = event.getContentIfNotHandled()) {
                is MessageEvent.Error -> {
                    if (m.message.toLowerCase().contains("unprocessable"))
                        makeToast(getString(R.string.too_long_message))
                    else makeToast(getString(R.string.error_message_not_send))
                }
                is MessageEvent.Created -> {
                }
                is MessageEvent.NoConnection -> {
                    if (!m.attempt.isNullOrBlank()) binding.input.editText?.setText(m.attempt)
                    makeToast(getString(R.string.no_connection))
                }
                is MessageEvent.CancelEditing -> {
                    cancelEditingMessage()
                }
                else -> {
                }
            }
        }

    }

    private fun refreshList() {
        adapter.lastItemPosition = 1
        if (adapter.lastItemPosition < NETWORK_PAGE_SIZE) {
            binding.list.scrollToPosition(0); return
        }
        viewModel.channel.value?.let {
            lifecycleScope.launch {
                adapter.submitData(PagingData.empty())
                search(channelId = it.channelId, refresh = true)
            }
        }
    }

    private fun cancelEditingMessage() {
        if (viewModel.isEditing.value == true) {
            unSelect?.invoke()
            unSelect = null
            viewModel.stopEditing()
            binding.send.setImageResource(R.drawable.ic_send)
            messageBeforeEdit = null
            messageEditingId = null
            binding.content.text?.clear()
        }
    }

    private fun sendMessage() {
        var message = binding.content.text.toString()
        if (message.isBlank() && (viewModel.isEditing.value == false)) return
        if (message.length > MAX_MESSAGE_LENGTH) message = message.slice(0 until MAX_MESSAGE_LENGTH)
        binding.content.text?.clear()
        if (viewModel.isEditing.value == true) {
            unSelect?.invoke()
            unSelect = null
            viewModel.stopEditing()
            binding.send.setImageResource(R.drawable.ic_send)
            // if edited message is the same as the one to be sent, save bandwidth and dismiss
            if (messageBeforeEdit != null && messageBeforeEdit == message) {
                messageBeforeEdit = null
                return
            }
            messageBeforeEdit = null
            messageEditingId?.let {
                if (message.isNotBlank())
                    viewModel.editMessage(it, message)
            }
            messageEditingId = null
        } else {
            created = true
            // todo set in a variable so in case of an error saved copy will be displayed
            viewModel.sendMessage(message)
        }
    }

    private fun openMenu(message: Message, anchor: View, unSelect: () -> Unit) {
        cancelEditingMessage()
        val popUpView = layoutInflater.inflate(R.layout.popup_message_menu, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(popUpView, width, height, true)
        var yoff = anchor.height
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)

        // difference between screen size and anchor location on y axis
        val ydiff = Resources.getSystem().displayMetrics.heightPixels - location[1]
        // if popup is going to be close to bottom nav bar, force yoff in opposite direction
        if ((ydiff.toFloat() + anchor.height) / Resources.getSystem().displayMetrics.heightPixels < 0.35f)
            yoff = -requireActivity().convertDpToPx(50f).toInt()

        popupWindow.elevation = requireContext().resources.getDimension(R.dimen.popup_elevation)
        popupWindow.showAtLocation(
            anchor,
            Gravity.NO_GRAVITY,
            location[0],
            location[1] + yoff
        )
        popUpView.findViewById<View>(R.id.copy)
            ?.setOnClickListener { copyTextToClipboard(message.content); popupWindow.dismiss() }

        popUpView.findViewById<View>(R.id.share_note)
            ?.setOnClickListener { shareText(message.content); popupWindow.dismiss() }

        popUpView.findViewById<View>(R.id.delete)
            ?.setOnClickListener { viewModel.deleteMessage(message.messageId); popupWindow.dismiss() }

        popupWindow.setOnDismissListener(unSelect)
    }

    private fun shareText(text: String) {
        requireActivity().hideSoftKeyboard()
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun copyTextToClipboard(text: String) {
        requireContext().copyToClipboard(text)
        makeToast(getString(R.string.text_copied))
    }


    private fun editMessage(message: Message, unSelect: () -> Unit, position: Int) {
        cancelEditingMessage()
        this.unSelect = unSelect
        viewModel.startEditing()
        messageBeforeEdit = message.content
        messageEditingId = message.messageId
        binding.content.setText(message.content)
        binding.content.requestFocus()
        requireContext().showKeyboard()
        binding.content.setSelection(binding.content.length())
        binding.send.setImageResource(R.drawable.ic_check_message_edit)
        lifecycleScope.launch {
            delay(600)
            val linearSmoothScroller: LinearSmoothScroller =
                object : LinearSmoothScroller(requireContext()) {
                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                        return 30f / displayMetrics.densityDpi
                    }
                }

            linearSmoothScroller.targetPosition = position

            binding.list.layoutManager?.startSmoothScroll(linearSmoothScroller)
        }
    }

    private fun initAdapter() {
        adapter = MessageAdapter(viewLifecycleOwner, ::openMenu, ::editMessage)
        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = MessagesLoadStateAdapter { adapter.retry() },
            footer = MessagesLoadStateAdapter { adapter.retry() }
        )
        adapter.addLoadStateListener { loadState ->

            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error

            // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Toast.makeText(
                    requireContext(),
                    "\uD83D\uDE28 Wooops",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun initSearch() {
        lifecycleScope.launch {
            adapter.dataRefreshFlow.collect {
                // if message has been created, scroll to the bottom
                if (created) {
                    created = false
                    refreshList()
                }
            }
        }
    }


    companion object {
        private const val SPACING: Float = 10f
        const val MAX_MESSAGE_LENGTH = 4000

    }

    private fun Context.copyToClipboard(text: CharSequence) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("content", text)
        clipboard.setPrimaryClip(clip)
    }
}

