package app.rootstock.views

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import app.rootstock.R
import app.rootstock.adapters.PatternAdapter
import app.rootstock.data.network.CreateOperation
import app.rootstock.databinding.DialogChannelCreateBinding
import app.rootstock.ui.channels.ChannelCreateViewModel
import app.rootstock.ui.channels.ColorsViewModel
import app.rootstock.ui.main.WorkspaceViewModel
import app.rootstock.utils.autoFitColumns
import app.rootstock.utils.convertDpToPx
import app.rootstock.utils.makeToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChannelCreateDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val spanCount = 4
        private const val ARGUMENT_WORKSPACE_ID = "ARGUMENT_WORKSPACE_ID"


        fun newInstance(wsId: String): ChannelCreateDialogFragment {
            return ChannelCreateDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGUMENT_WORKSPACE_ID, wsId)
                }
            }
        }
    }

    private var wsId: String? = null

    private val editViewModel: ColorsViewModel by viewModels()

    private lateinit var binding: DialogChannelCreateBinding

    private val viewModel: ChannelCreateViewModel by viewModels()

    private val adapterToSet =
        PatternAdapter(items = mutableListOf(), ::patternClicked, true, circle = true)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogChannelCreateBinding.inflate(layoutInflater, container, true)
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
        image ?: return
        viewModel.channel.value?.imageUrl = image
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        wsId = arguments?.getString(ARGUMENT_WORKSPACE_ID)
        binding.apply {
            viewmodel = this@ChannelCreateDialogFragment.viewModel
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
        binding.save.setOnClickListener {
            wsId?.let { id -> viewModel.createChannel(id) }
        }
        binding.cancel.setOnClickListener { dismiss() }

        viewModel.eventChannel.observe(viewLifecycleOwner) {
            if (it != null) {
                val op = it.getContentIfNotHandled() ?: return@observe
                when (op) {
                    is CreateOperation.Success -> {
                        op.obj?.let { c ->
                            val main: WorkspaceViewModel by activityViewModels()
                            main.addChannel(c)
                        }
                    }
                    is CreateOperation.Error -> {
                        makeToast(getString(R.string.error_channel_not_created))
                    }
                }
                dismiss()
            }
        }
    }

}