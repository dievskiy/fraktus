package app.rootstock.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import app.rootstock.R
import app.rootstock.databinding.MessagesLoadStateBinding

class MessagesLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<MessagesLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: MessagesLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): MessagesLoadStateViewHolder {
        return MessagesLoadStateViewHolder.create(parent, retry)
    }
}



class MessagesLoadStateViewHolder(
    private val binding: MessagesLoadStateBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener { retry.invoke() }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.errorMsg.text = loadState.error.localizedMessage
        }
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState !is LoadState.Loading
        binding.errorMsg.isVisible = loadState !is LoadState.Loading
    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): MessagesLoadStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.messages_load_state, parent, false)
            val binding = MessagesLoadStateBinding.bind(view)
            return MessagesLoadStateViewHolder(binding, retry)
        }
    }
}
