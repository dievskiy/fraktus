package app.rootstock.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.rootstock.data.channel.Channel
import app.rootstock.databinding.ItemChannelFavouriteBinding

class ChannelFavouritesAdapter constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val openChannel: (channel: Channel) -> Unit,
) :
    ListAdapter<Channel, ChannelFavouritesAdapter.ChannelFavouriteViewHolder>(
        object :
            DiffUtil.ItemCallback<Channel>() {

            override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem.channelId == newItem.channelId
            }

            override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem == newItem
            }

        }) {

    inner class ChannelFavouriteViewHolder constructor(
        private val binding: ItemChannelFavouriteBinding,
        private val lifecycleOwner: LifecycleOwner,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Channel) {
            binding.channel = item
            binding.lifecycleOwner = lifecycleOwner
            binding.container.setOnClickListener { openChannel(item) }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelFavouriteViewHolder {
        val binding = ItemChannelFavouriteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChannelFavouriteViewHolder(binding, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: ChannelFavouriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

