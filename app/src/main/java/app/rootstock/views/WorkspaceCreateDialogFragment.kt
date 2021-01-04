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
import app.rootstock.databinding.DialogCreateWorkspaceBinding
import app.rootstock.ui.channels.ColorsViewModel
import app.rootstock.ui.main.WorkspaceViewModel
import app.rootstock.ui.workspace.WorkspaceCreateViewModel
import app.rootstock.utils.autoFitColumns
import app.rootstock.utils.convertDpToPx
import app.rootstock.utils.makeToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class WorkspaceCreateDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val spanCount = 4
        private const val INTENT_EXTRAS_WS_ID = "wsId"

        fun newInstance(wsId: String): WorkspaceCreateDialogFragment {
            val bundle = Bundle().apply {
                putString(INTENT_EXTRAS_WS_ID, wsId)
            }
            return WorkspaceCreateDialogFragment().apply { arguments = bundle }
        }
    }

    private val colorsViewModel: ColorsViewModel by viewModels()

    private var wsId: String? = null

    private val viewModel: WorkspaceCreateViewModel by viewModels()

    private lateinit var binding: DialogCreateWorkspaceBinding

    private val adapterToSet =
        PatternAdapter(
            items = mutableListOf(), ::patternClicked, true, circle = false
        )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCreateWorkspaceBinding.inflate(layoutInflater, container, true)
        return binding.root
    }

    private fun patternClicked(position: Int, image: String?) {
        binding.colorsRv.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ChannelPickImageView>(
            R.id.color_item
        )?.togglePicked()

        image?.let { viewModel.setImage(it) }

        // unpick previously picked color
        if (adapterToSet.previousPickedPosition != null && position != adapterToSet.previousPickedPosition) {
            adapterToSet.previousPickedPosition?.let {
                binding.colorsRv.findViewHolderForAdapterPosition(it)?.itemView?.findViewById<ChannelPickImageView>(
                    R.id.color_item
                )?.unPick()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        wsId = arguments?.getString(INTENT_EXTRAS_WS_ID)
        binding.apply {
            viewmodel = this@WorkspaceCreateDialogFragment.viewModel
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
        colorsViewModel.images.observe(this) {
            adapterToSet.updateList(it.urls)
        }

        if (showsDialog) {
            (requireDialog() as AlertDialog).setView(binding.root)
        }
        binding.save.setOnClickListener { createWorkspace() }
        binding.cancel.setOnClickListener { dismiss() }


    }

    private fun createWorkspace() {
        if (wsId == null) dismiss()
        //todo dd
        viewModel.event.observe(this) {
            when (val content = it.getContentIfNotHandled()) {
                is CreateOperation.Success -> {
                    val main: WorkspaceViewModel by activityViewModels()
                    content.obj?.let { w -> main.createWorkspace(w) }
                    dismiss()
                }
                is CreateOperation.Error -> {
                    makeToast(getString(R.string.error_workspace_not_created))
                    val main: WorkspaceViewModel by activityViewModels()
                    main.removeWorkspace(workspaceId = requireNotNull(wsId))
                    dismiss()
                }
                null -> {
                }
            }
        }
        viewModel.createWorkspace(wsId = requireNotNull(wsId))
    }

}