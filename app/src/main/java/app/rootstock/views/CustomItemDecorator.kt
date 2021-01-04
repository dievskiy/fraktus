package app.rootstock.views


import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class DrawableItemDecorator(private val mDivider: Drawable) :
    ItemDecoration() {
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0..childCount - 2) {
            val child: View = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop: Int = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + mDivider.intrinsicHeight
            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            mDivider.draw(canvas)
        }
    }
}

/**
 * Spacing decorator for [RecyclerView], where order is reversed
 */
class SpacingItemDecorationReversed(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.top = space

        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.bottom = space
        }
    }
}

class SpacingItemDecoration @JvmOverloads constructor(
    private val startSpacing: Int = 0,
    private val endSpacing: Int = 0
) :
    ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (startSpacing <= 0 || endSpacing <= 0) {
            return
        }
        if ((startSpacing != 0) && parent.getChildLayoutPosition(view) < 1 || parent.getChildLayoutPosition(
                view
            ) >= 1
        ) {
            if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
                outRect.top = startSpacing
            } else {
                outRect.left = startSpacing
            }
        }
        if ((endSpacing != 0) && parent.getChildAdapterPosition(view) == getTotalItemCount(parent) - 1) {
            if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
                outRect.bottom = endSpacing
            } else {
                outRect.right = endSpacing
            }
        }
    }

    private fun getTotalItemCount(parent: RecyclerView): Int {
        return parent.adapter!!.itemCount
    }

    private fun getOrientation(parent: RecyclerView): Int {
        return if (parent.layoutManager is LinearLayoutManager) {
            (parent.layoutManager as LinearLayoutManager?)!!.orientation
        } else {
            throw IllegalStateException("SpacingItemDecoration can only be used with a LinearLayoutManager.")
        }
    }
}