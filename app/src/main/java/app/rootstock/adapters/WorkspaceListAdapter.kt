package app.rootstock.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.rootstock.data.channel.Channel
import app.rootstock.data.workspace.Workspace
import app.rootstock.data.workspace.WorkspaceI
import app.rootstock.databinding.ItemWorkspaceBinding

interface WorkspaceEventHandler {
    fun workspaceClicked(workspaceId: String)
}

class WorkspaceListAdapter constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val workspaceEventHandler: WorkspaceEventHandler,
    private val editDialog: (v: View, w: WorkspaceI) -> Unit,
    ) :
    androidx.recyclerview.widget.ListAdapter<Workspace, WorkspaceListAdapter.WorkspaceViewHolder>(
        object :
            DiffUtil.ItemCallback<Workspace>() {

            override fun areItemsTheSame(oldItem: Workspace, newItem: Workspace): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Workspace, newItem: Workspace): Boolean {
                return oldItem == newItem
            }

        }) {

    inner class WorkspaceViewHolder constructor(
        private val binding: ItemWorkspaceBinding,
        private val lifecycleOwner: LifecycleOwner,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Workspace) {
            binding.workspace = item
            binding.workspaceEventHandler = workspaceEventHandler
            binding.lifecycleOwner = lifecycleOwner
            binding.editWorkspace.setOnClickListener { editDialog(it, item) }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkspaceViewHolder {
        val binding = ItemWorkspaceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkspaceViewHolder(binding, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: WorkspaceViewHolder, position: Int) {
        holder.bind(getItem(position))

    }
}
