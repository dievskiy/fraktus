package app.rootstock.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.rootstock.R
import app.rootstock.utils.GlideApp.with
import app.rootstock.views.ChannelPickImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class PatternAdapter constructor(
    private val items: MutableList<String>,
    private val patternClicked: ((position: Int, image: String?) -> Unit),
    // for create dialog we want to preselect color and be sure, that
    // there is always an image attached to entity.
    private val selectFirst: Boolean = false,
    private val circle: Boolean = true
) : RecyclerView.Adapter<PatternAdapter.PatternViewHolder>() {

    var previousPickedPosition: Int? = null

    companion object {
        private const val ROUNDED_CORNERS = 40
    }

    inner class PatternViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: String, position: Int) {
            itemView.findViewById<ChannelPickImageView>(R.id.color_item)
                ?.let {
                    when (circle) {
                        true -> {
                            with(it)
                                .load(item)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .circleCrop()
                                .placeholder(R.drawable.circle_channel)
                                .error(R.drawable.circle_channel)
                                .into(it)
                        }
                        false -> {
                            with(it)
                                .applyDefaultRequestOptions(
                                    RequestOptions().transform(
                                        RoundedCorners(ROUNDED_CORNERS),
                                        CenterCrop()
                                    )
                                )
                                .load(item)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .placeholder(R.drawable.placeholder_workspace)
                                .error(R.drawable.placeholder_workspace)
                                .into(it)
                        }
                    }

                    it.setOnClickListener {
                        if (previousPickedPosition == position && selectFirst) return@setOnClickListener
                        else if (previousPickedPosition == position) {
                            patternClicked(position, null)
                            return@setOnClickListener
                        }
                        patternClicked(position, item)
                        previousPickedPosition = position
                    }
                    // preselect first element
                    if (selectFirst && position == 0) {
                        it.togglePicked()
                        patternClicked(position, item)
                        previousPickedPosition = position
                    }
                }
        }
    }

    fun updateList(newData: List<String>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatternViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_color_channel, parent, false)
        return PatternViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatternViewHolder, position: Int) {
        val item = items.getOrNull(position) ?: return
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = items.size
}
