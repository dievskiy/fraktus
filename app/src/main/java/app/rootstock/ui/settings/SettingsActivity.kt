package app.rootstock.ui.settings

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import app.rootstock.R
import app.rootstock.adapters.SettingsListAdapter
import app.rootstock.databinding.ActivitySettingsBinding
import app.rootstock.di.modules.AppModule.Companion.URL_PRIVACY_POLICY
import app.rootstock.ui.signup.RegisterActivity
import app.rootstock.views.DeleteAccountDialogFragment
import app.rootstock.views.DrawableItemDecorator
import app.rootstock.views.LogOutDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        setRv()
        setObservers()
        setToolbar()

    }

    private fun setRv() {
        val items = listOf(
            SettingsItem(
                drawable = R.drawable.ic_baseline_security_24,
                title = getString(R.string.privacy_policy),
                actionHandler = ::privacyClicked
            ),
            SettingsItem(
                drawable = R.drawable.ic_logout_24,
                title = getString(R.string.settings_log_out),
                actionHandler = ::showSignOutDialog
            ),
            SettingsItem(
                drawable = R.drawable.ic_delete_forever_24,
                title = getString(R.string.delete_account),
                actionHandler = ::showDeleteDialog
            ),
        )

        val adapterToSet = SettingsListAdapter(items = items, lifecycleOwner = this)
        binding.rv.apply {
            adapter = adapterToSet
            ContextCompat.getDrawable(this@SettingsActivity, R.drawable.divider_channels)
                ?.let {
                    addItemDecoration(DrawableItemDecorator(it))
                }

        }
    }

    private fun privacyClicked() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_PRIVACY_POLICY))
        startActivity(intent)
    }

    private fun showSignOutDialog() {
        val dialog = LogOutDialogFragment()
        dialog.show(supportFragmentManager, DIALOG_LOG_OUT)
    }

    private fun showDeleteDialog() {
        val email = viewModel.userData.value?.email ?: return
        val dialog = DeleteAccountDialogFragment.newInstance(email)
        dialog.show(supportFragmentManager, DIALOG_DELETE_ACCOUNT)

    }

    private fun setToolbar() {
        binding.toolbar.apply {
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            navigationIcon?.setTint(Color.BLACK)
            setNavigationOnClickListener { onBackPressed() }
        }
    }

    private fun setObservers() {
        viewModel.event.observe(this) {
            when (it?.getContentIfNotHandled()) {
                SettingsEvent.LOG_OUT -> startRegisterActivity()
                SettingsEvent.DELETED -> startRegisterActivity(true)
                SettingsEvent.FAILED -> {
                    Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
        }
    }

    private fun startRegisterActivity(deleted: Boolean? = null) {
        val i = Intent(applicationContext, RegisterActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        deleted?.let { i.putExtra(ACCOUNT_DELETED, it) }
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
        finishAfterTransition()
    }

    companion object {
        // todo real site link
        const val DIALOG_LOG_OUT = "DIALOG_LOG_OUT"
        const val DIALOG_DELETE_ACCOUNT = "DIALOG_DELETE_ACCOUNT"
        const val ACCOUNT_DELETED = "ACCOUNT_DELETED"
    }

}

class SettingsItem(
    @DrawableRes val drawable: Int,
    val title: String,
    val actionHandler: () -> Unit
)