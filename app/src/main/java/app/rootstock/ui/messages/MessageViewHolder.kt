package app.rootstock.ui.messages

import android.graphics.drawable.TransitionDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import app.rootstock.R
import app.rootstock.data.messages.Message
import app.rootstock.databinding.ItemMessageBinding


class MessageViewHolder(
    private val binding: ItemMessageBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val openMenu: (message: Message, anchor: View, unSelect: () -> Unit) -> Unit,
    private val edit: (message: Message, unSelect: () -> Unit, position: Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(message: Message?, position: Int) {
        binding.lifecycleOwner = lifecycleOwner
        message?.let { m ->
            binding.message = m
            binding.messageContainer.setOnClickListener {
                openMenu(m, binding.messageContainer, ::unSelect)
                select()
            }
            binding.edit.setOnClickListener { edit(m, ::unSelect, position); select(); }
        }
        binding.executePendingBindings()
    }

    private fun select() {
        val context = binding.messageContainer.context ?: return
        val drawable = binding.messageContainer.background as TransitionDrawable
        drawable.startTransition(200)
        binding.content.setTextColor(context.getColor(R.color.white))
    }

    private fun unSelect() {
        val context = binding.messageContainer.context ?: return
        val drawable = binding.messageContainer.background as TransitionDrawable
        drawable.reverseTransition(150)
        binding.content.setTextColor(context.getColor(R.color.black))
    }

}
