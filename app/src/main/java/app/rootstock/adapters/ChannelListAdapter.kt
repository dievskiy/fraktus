package app.rootstock.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.rootstock.R
import app.rootstock.data.channel.Channel
import app.rootstock.databinding.ItemChannelBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ChannelListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val editDialog: (v: View, c: Channel, card: View) -> Unit,
    private val openChannel: (channel: Channel) -> Unit
) : ListAdapter<Channel, ChannelListAdapter.ChannelViewHolder>(
    object :
        DiffUtil.ItemCallback<Channel>() {

        override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return oldItem.channelId == newItem.channelId
        }

        override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return oldItem == newItem
        }

    }) {

    inner class ChannelViewHolder constructor(
        private val binding: ItemChannelBinding,
        private val lifecycleOwner: LifecycleOwner,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Channel) {
            binding.channels = item
            binding.lifecycleOwner = lifecycleOwner
            binding.executePendingBindings()
            item.imageUrl?.let {
                Glide.with(binding.channelColor.context)
                    .applyDefaultRequestOptions(
                        RequestOptions().circleCrop().placeholder(R.drawable.circle_channel)
                            .error(R.drawable.circle_channel)
                    )
                    .load(it)
                    .into(binding.channelColor)
            }

            binding.channelEdit.setOnClickListener {
                editDialog(it, item, binding.card)
            }
            binding.card.setOnClickListener { openChannel(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ItemChannelBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChannelViewHolder(binding, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

