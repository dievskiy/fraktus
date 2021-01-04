package app.rootstock.ui.workspace

import android.animation.ObjectAnimator
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import app.rootstock.R
import app.rootstock.adapters.WorkspaceListAdapter
import app.rootstock.data.workspace.WorkspaceI
import app.rootstock.databinding.FragmentWorkspaceListBinding
import app.rootstock.ui.main.WorkspaceActivity
import app.rootstock.ui.main.WorkspaceViewModel
import app.rootstock.utils.InternetUtil
import app.rootstock.utils.autoFitColumns
import app.rootstock.utils.convertDpToPx
import app.rootstock.utils.makeToast
import app.rootstock.views.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class WorkspaceListFragment : Fragment() {

    private val viewModel: WorkspaceViewModel by activityViewModels()

    private lateinit var binding: FragmentWorkspaceListBinding

    lateinit var adapter: WorkspaceListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWorkspaceListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = WorkspaceListAdapter(
            lifecycleOwner = this,
            workspaceEventHandler = viewModel,
            ::openEditDialog
        )
        binding.recyclerView.apply {
            adapter = this@WorkspaceListFragment.adapter
            autoFitColumns(WORKSPACE_COLUMN_WIDTH_DP, WORKSPACE_SPAN_COUNT)
            // inner and bottom-element padding are same
            addItemDecoration(
                GridSpacingItemDecoratorWithCustomCenter(
                    spanCount = WORKSPACE_SPAN_COUNT,
                    spacing = requireContext().convertDpToPx(20f).toInt(),
                    centerSpacing = requireContext().convertDpToPx(10f).toInt(),
                    bottomSpacing = requireContext().convertDpToPx(30f).toInt()
                )
            )
            setPadding(
                paddingLeft,
                paddingTop,
                paddingRight,
                requireContext().convertDpToPx(50f).toInt()
            )
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
        viewModel.workspacesChildren.observe(viewLifecycleOwner) {
            if (it != null) {
                if (::adapter.isInitialized) {
                    if (it.isEmpty()) {
                        lifecycleScope.launch {
                            delay(500)
                            if (viewModel.workspacesChildren.value.isNullOrEmpty()) {
                                binding.noWorkspaces.isVisible = true
                                binding.noWorkspacesText.isVisible = true
                            }
                        }
                    } else {
                        binding.noWorkspaces.isVisible = false
                        binding.noWorkspacesText.isVisible = false
                    }
                    adapter.submitList(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun openEditDialog(anchor: View, workspace: WorkspaceI) {
        viewModel.editDialogOpened()
        showEditPopup(anchor, workspace)
    }

    private fun showEditPopup(anchor: View, workspace: WorkspaceI) {
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
        if (ydiff.toFloat() / Resources.getSystem().displayMetrics.heightPixels < 0.25f) yoff =
            -requireContext().convertDpToPx(120f).toInt()

        popupWindow.showAsDropDown(anchor, 0, yoff)
        popupWindow.setOnDismissListener { viewModel.editChannelStop() }

        popUpView.findViewById<TextView>(R.id.edit_text)?.text = getString(R.string.edit_workspace)
        popUpView.findViewById<TextView>(R.id.delete_text)?.text =
            getString(R.string.delete_workspace)

        popUpView.findViewById<View>(R.id.edit)?.setOnClickListener {
            popupWindow.dismiss()
            val dialog = WorkspaceEditDialogFragment.newInstance(workspace)
            dialog.show(requireActivity().supportFragmentManager, DIALOG_WORKSPACE_EDIT)
        }
        popUpView.findViewById<View>(R.id.delete)?.setOnClickListener {
            popupWindow.dismiss()
            val content = getString(
                R.string.delete_workspace_body, workspace.name
            )
            val deleteObj = DeleteObj(
                content = content,
                id = workspace.workspaceId,
                delete = ::delete,
                deleteType = ItemType.WORKSPACE,
                bold = Pair(32, content.length)
            )
            val dialog = DeleteDialogFragment(deleteObj)
            dialog.show(
                requireActivity().supportFragmentManager,
                DIALOG_WORKSPACE_DELETE
            )
        }
    }

    private fun delete(wsId: String) {
        if (!InternetUtil.isInternetOn()) {
            makeToast(getString(R.string.no_connection))
            return
        }
        viewModel.deleteWorkspace(wsId)
    }

    companion object {
        const val WORKSPACE_SPAN_COUNT = 2
        const val WORKSPACE_COLUMN_WIDTH_DP = 100
        const val DIALOG_WORKSPACE_DELETE = "DIALOG_WORKSPACE_DELETE"
        const val DIALOG_WORKSPACE_EDIT = "DIALOG_WORKSPACE_EDIT"
    }


}