package app.rootstock.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import app.rootstock.databinding.ItemSettingsBinding
import app.rootstock.ui.settings.SettingsItem


class SettingsListAdapter constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val items: List<SettingsItem>
) : RecyclerView.Adapter<SettingsListAdapter.SettingPreferenceViewHolder>() {

    inner class SettingPreferenceViewHolder constructor(
        private val binding: ItemSettingsBinding,
        private val lifecycleOwner: LifecycleOwner,
        ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem) {
            binding.action = item.actionHandler
            binding.title = item.title
            binding.iconId = item.drawable
            binding.lifecycleOwner = lifecycleOwner
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingPreferenceViewHolder {
        val binding = ItemSettingsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SettingPreferenceViewHolder(binding, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: SettingPreferenceViewHolder, position: Int) {
        val item = items.getOrNull(position) ?: return
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size
}

