package app.rootstock.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.updateBounds
import app.rootstock.R

class ChannelPickImageView(context: Context, attributeSet: AttributeSet) :
    androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    var isPicked = false

    fun togglePicked() {
        isPicked = !isPicked
        foreground = if (isPicked) {
            ContextCompat.getDrawable(context, R.drawable.ic_check_24)
        } else {
            null
        }
    }

    init {
        foregroundGravity = Gravity.CENTER
    }

    fun unPick() {
        isPicked = false
        foreground = null
    }
}