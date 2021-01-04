package app.rootstock.ui.signup

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import app.rootstock.R
import app.rootstock.databinding.ActivityAccountBinding
import androidx.databinding.DataBindingUtil.setContentView
import app.rootstock.ui.settings.SettingsActivity.Companion.ACCOUNT_DELETED
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivityAccountBinding>(this, R.layout.activity_account)

        // show deleted dialog in case user has just deleted it
        if (intent.getBooleanExtra(ACCOUNT_DELETED, false)) {
            val view = layoutInflater.inflate(R.layout.dialog_account_deleted, null)
            val dialog = MaterialAlertDialogBuilder(this).create()
            view.findViewById<View>(R.id.ok)?.setOnClickListener { dialog.dismiss() }
            dialog.setView(view)
            dialog.show()
        }
    }

}