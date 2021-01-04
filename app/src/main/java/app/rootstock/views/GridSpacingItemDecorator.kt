package app.rootstock.views

import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class GridSpacingItemDecorator(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean,
    private val bottomSameAsSide: Boolean? = false
) :
    ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column
        if (includeEdge) {
            outRect.left =
                spacing - column * spacing / spanCount
            outRect.right =
                (column + 1) * spacing / spanCount
            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right =
                spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {

                outRect.top = if (bottomSameAsSide == true) spacing % 2 else spacing
            }
        }
    }
}


class GridSpacingItemDecoratorWithCustomCenter(
    private val spanCount: Int,
    private val spacing: Int,
    private val centerSpacing: Int,
    private val bottomSpacing: Int = spacing,

    ) :
    ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column
        outRect.left =
            if (position % 2 == 1) column * (spacing + centerSpacing) / spanCount else column * spacing / spanCount
        outRect.right =
            if (position % 2 == 1) spacing - (column + 1) * spacing / spanCount else spacing - (column + 1) * (spacing - centerSpacing) / spanCount

        if (position >= spanCount) {
            outRect.top = bottomSpacing // item top
        }
    }
}
