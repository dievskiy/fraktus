package app.rootstock.ui.launch

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import app.rootstock.R
import app.rootstock.ui.main.WorkspaceActivity
import app.rootstock.ui.signup.RegisterActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {
    val viewModel: LaunchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.launchDestination.observe(this) {
            when (it.getContentIfNotHandled()) {
                Launch.WORKSPACE_ACTIVITY -> startWorkspaceActivity()
                Launch.SIGN_UP_ACTIVITY -> startSignInActivity()
                null -> startSignInActivity()
            }
        }
    }

    private fun startWorkspaceActivity() {
        val intent = Intent(this, WorkspaceActivity::class.java)
        startActivity(intent)
        finishAfterTransition()
    }

    private fun startSignInActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finishAfterTransition()
    }
}