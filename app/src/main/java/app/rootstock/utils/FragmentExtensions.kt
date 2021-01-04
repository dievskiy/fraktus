package app.rootstock.utils

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.makeToast(message: String, long: Boolean = false) {
    val duration = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this.context, message, duration).show()
}