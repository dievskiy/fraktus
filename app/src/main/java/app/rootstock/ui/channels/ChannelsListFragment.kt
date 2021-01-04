package app.rootstock.ui.channels

import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import app.rootstock.R
import app.rootstock.adapters.ChannelListAdapter
import app.rootstock.data.channel.Channel
import app.rootstock.data.result.Event
import app.rootstock.databinding.FragmentChannelsListBinding
import app.rootstock.ui.main.WorkspaceActivity.Companion.BUNDLE_CHANNEL_EXTRA
import app.rootstock.ui.main.WorkspaceActivity.Companion.REQUEST_CODE_CHANNEL_ACTIVITY
import app.rootstock.ui.main.WorkspaceViewModel
import app.rootstock.ui.messages.MessageEvent
import app.rootstock.ui.messages.MessagesFragment
import app.rootstock.ui.messages.MessagesViewModel
import app.rootstock.utils.InternetUtil
import app.rootstock.utils.convertDpToPx
import app.rootstock.utils.makeToast
import app.rootstock.views.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChannelsListFragment : Fragment() {

    private val viewModel: WorkspaceViewModel by activityViewModels()

    private lateinit var binding: FragmentChannelsListBinding

    lateinit var adapter: ChannelListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChannelsListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    private fun openEditDialog(anchor: View, channel: Channel, card: View) {
        viewModel.editDialogOpened()
        showEditPopup(anchor, channel, card)
    }

    private fun openChannel(channel: Channel) {
        // if there is send action from other app, show dialog
        requireActivity().intent.getStringExtra(Intent.EXTRA_TEXT)?.let { message ->
            showSendSharedTextDialog(message, channel)
            return
        }
        // otherwise start channel activity
        val intent = Intent(requireActivity(), ChannelActivity::class.java)
        intent.putExtra(BUNDLE_CHANNEL_EXTRA, channel)
        requireActivity().startActivityForResult(intent, REQUEST_CODE_CHANNEL_ACTIVITY)
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
            val observer = Observer<Event<MessageEvent>> {
                when (val response = it?.getContentIfNotHandled()) {
                    is MessageEvent.Error -> {
                        if (response.message.toLowerCase().contains("unprocessable"))
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
            messageViewModel.messageEvent.observe(this, observer)

        }
        dialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChannelListAdapter(
            lifecycleOwner = this,
            editDialog = ::openEditDialog,
            openChannel = ::openChannel
        )
        binding.recyclerView.adapter = adapter

        val itemDecorator =
            SpacingItemDecoration(
                endSpacing = requireContext().convertDpToPx(dp = CHANNEL_END_SPACING).toInt(),
                startSpacing = requireContext().convertDpToPx(dp = CHANNEL_START_SPACING).toInt(),
            )
        binding.recyclerView.apply {
            addItemDecoration(itemDecorator)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    viewModel.pageScrolled()
                }
            })
        }
        setObservers()
    }


    private fun setObservers() {
        viewModel.channels.observe(viewLifecycleOwner) {
            if (it != null && ::adapter.isInitialized) {
                if (it.isEmpty()) {
                    binding.noChannels.isVisible = true
                    binding.noChannelsText.isVisible = true
                } else {
                    binding.noChannels.isVisible = false
                    binding.noChannelsText.isVisible = false
                }
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                binding.recyclerView.scrollToPosition(0)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                binding.recyclerView.scrollToPosition(0)
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                binding.recyclerView.scrollToPosition(0)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.recyclerView.scrollToPosition(0)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                binding.recyclerView.scrollToPosition(0)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                binding.recyclerView.scrollToPosition(0)
            }
        })
    }

    private fun showEditPopup(anchor: View, channel: Channel, card: View) {
        val popUpView = layoutInflater.inflate(R.layout.popup_channel_menu, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(popUpView, width, height, true)
        var yoff = 0
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)

        // difference between screen size and anchor location on y axis
        val ydiff = Resources.getSystem().displayMetrics.heightPixels - location[1]
        // if popup is going to be close to bottom nav bar, force yoff in opposite direction
        if (ydiff.toFloat() / Resources.getSystem().displayMetrics.heightPixels < 0.35f) yoff =
            -requireContext().convertDpToPx(180f).toInt()

        popupWindow.showAsDropDown(anchor, 0, yoff)
        popupWindow.setOnDismissListener { viewModel.editChannelStop() }

        popUpView.findViewById<View>(R.id.edit)?.setOnClickListener {
            popupWindow.dismiss()
            val dialog = ChannelEditDialogFragment.newInstance(channel, false)
            dialog.show(requireActivity().supportFragmentManager, DIALOG_CHANNEL_EDIT)
        }
        popUpView.findViewById<View>(R.id.delete)?.setOnClickListener {
            popupWindow.dismiss()
            val content = getString(
                R.string.delete_channel_body, channel.name
            )
            val deleteObj = DeleteObj(
                content = content,
                delete = ::deleteChannel,
                id = channel.channelId,
                deleteType = ItemType.CHANNEL,
                bold = Pair(32, content.length - 1)
            )
            val dialog = DeleteDialogFragment(deleteObj)

            dialog.show(requireActivity().supportFragmentManager, DIALOG_CHANNEL_DELETE)
        }
    }

    private fun deleteChannel(channelId: Long) {
        if (!InternetUtil.isInternetOn()) {
            makeToast(getString(R.string.no_connection))
            return
        }
        viewModel.deleteChannel(channelId)
    }

    companion object {
        private const val DIALOG_CHANNEL_EDIT = "dialog_channel_edit"
        private const val DIALOG_CHANNEL_DELETE = "dialog_channel_delete"
        private const val CHANNEL_START_SPACING = 15f
        private const val CHANNEL_END_SPACING = 25f

    }

}
