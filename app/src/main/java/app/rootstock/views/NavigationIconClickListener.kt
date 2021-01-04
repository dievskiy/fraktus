package app.rootstock.views


import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.Interpolator
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import app.rootstock.R
import app.rootstock.utils.convertDpToPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NavigationIconClickListener @JvmOverloads internal constructor(
    context: Context,
    private val backView: View,
    private val sheet: View,
    private val backdropSize: Int,
    private val animDuration: Long = 200L,
    private val interpolator: Interpolator? = null,
    private val openIcon: Drawable? = null,
    private val closeIcon: Drawable? = null,
    private val toolbar: Toolbar
) : View.OnClickListener {

    private val animatorSet = AnimatorSet()
    private var backdropShown = false
    private var toolbarNavIcon: AppCompatImageButton? = null

    init {
        try {
            toolbarNavIcon = toolbar[0] as AppCompatImageButton
        } catch (e: Exception) {
        }
    }

    fun open() = if (!backdropShown) {
        toolbarNavIcon?.let { onClick(it) }
    } else {
    }

    fun close() = if (backdropShown) {
        toolbarNavIcon?.let { onClick(it) }
    } else {
    }

    override fun onClick(view: View) {
        // only bind once
        if (toolbarNavIcon == null) {
            toolbarNavIcon = view as AppCompatImageButton
        }

        backdropShown = !backdropShown

        val translateY = backView.height - 56 // todo change
        // Cancel the existing animations
        animatorSet.removeAllListeners()
        animatorSet.end()
        animatorSet.cancel()

        updateIcon()

        // start animation
        val animator = ObjectAnimator.ofFloat(
            sheet,
            "translationY",
            (if (backdropShown) translateY else 0).toFloat()
        )
        animator.duration = animDuration
        interpolator?.let { interpolator ->
            animator.interpolator = interpolator
        }

        // play the animation
        animatorSet.play(animator)
        animator.start()

    }

    /**
     * Update the Toolbar icon
     * @param view the clicked view. This must be a ImageView.
     */
    private fun updateIcon() {
        toolbarNavIcon?.let {
            if (openIcon != null && closeIcon != null) {
                when (backdropShown) {
                    true -> it.setImageDrawable(closeIcon)
                    false -> it.setImageDrawable(openIcon)
                }
            }
        }
    }
}