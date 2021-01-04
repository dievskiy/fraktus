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
import app.rootstock.data.workspace.Workspace
import app.rootstock.data.workspace.WorkspaceConstants.workspaceNameRange
import app.rootstock.data.workspace.WorkspaceI
import app.rootstock.databinding.DialogWorkspaceEditBinding
import app.rootstock.ui.channels.ColorsViewModel
import app.rootstock.ui.main.WorkspaceViewModel
import app.rootstock.utils.InternetUtil
import app.rootstock.utils.autoFitColumns
import app.rootstock.utils.convertDpToPx
import app.rootstock.utils.makeToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_workspace_edit.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Dialog that appears when editing workspace.
 */
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class WorkspaceEditDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val spanCount = 4
        private const val ARGUMENT_WORKSPACE = "ARGUMENT_CHANNEL"

        fun newInstance(workspace: WorkspaceI): WorkspaceEditDialogFragment {
            return WorkspaceEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARGUMENT_WORKSPACE, workspace)
                }
            }
        }
    }

    private var imageUrl: String? = null

    private lateinit var binding: DialogWorkspaceEditBinding

    private val viewModel: WorkspaceViewModel by activityViewModels()

    private val editViewModel: ColorsViewModel by viewModels()

    private var workspace: WorkspaceI? = null

    private val adapterToSet =
        PatternAdapter(items = mutableListOf(), ::patternClicked, circle = false, selectFirst = false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).create()
    }

    // todo check not loaded patterns click
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogWorkspaceEditBinding.inflate(layoutInflater, container, true)
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
        imageUrl = image
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        workspace = arguments?.getSerializable(ARGUMENT_WORKSPACE) as? WorkspaceI
        binding.apply {
            workspace = this@WorkspaceEditDialogFragment.workspace
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
        binding.workspaceEditNameText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.save.isEnabled = p0 != null && p0.length in workspaceNameRange
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
            workspace?.let { w ->
                val newName =
                    view?.findViewById<EditText>(R.id.workspace_edit_name_text)?.text?.toString()
                        ?: return@setOnClickListener
                // if image has not been changed - use initial image
                if (imageUrl == null) imageUrl = w.imageUrl
                val newWorkspace = Workspace(
                    workspaceId = w.workspaceId,
                    name = newName,
                    backgroundColor = w.backgroundColor,
                    imageUrl = imageUrl,
                    createdAt = w.createdAt,
                )
                viewModel.updateWorkspace(newWorkspace)
            }
            dismiss()
        }
        binding.cancel.setOnClickListener { dismiss() }
    }

}
