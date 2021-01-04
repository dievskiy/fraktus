package app.rootstock.views

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.google.android.material.textfield.TextInputEditText

/**
 * Base class for TextInputEditText
 */
open class BaseInputEditText : TextInputEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        // clear focus if back button pressed
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus()
        }

        return super.onKeyPreIme(keyCode, event)
    }
}

