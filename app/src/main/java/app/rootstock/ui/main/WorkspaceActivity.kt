package app.rootstock.ui.main

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import app.rootstock.R
import app.rootstock.data.network.ReLogInObservable
import app.rootstock.data.network.ReLogInObserver
import app.rootstock.databinding.ActivityMainWorkspaceBinding
import app.rootstock.ui.channels.FavouriteChannelsFragment
import app.rootstock.ui.launch.LauncherActivity
import app.rootstock.ui.settings.SettingsActivity
import app.rootstock.ui.signup.RegisterActivity
import app.rootstock.ui.update.UpdateActivity
import app.rootstock.utils.convertDpToPx
import app.rootstock.views.ChannelCreateDialogFragment
import app.rootstock.views.WorkspaceCreateDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main_workspace.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WorkspaceActivity : AppCompatActivity(), ReLogInObserver {

    private val viewModel: WorkspaceViewModel by viewModels()

    @Inject
    lateinit var reLogInObservable: ReLogInObservable

    private lateinit var binding: ActivityMainWorkspaceBinding

    private var isInShareMessageMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_workspace)

        setClickListeners()
        setToolbar()
        setObservers()
        checkSendData()
    }

    private fun checkSendData() {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    isInShareMessageMode = true
                    binding.homeToolbar.title = "select channel"
                }
            }
            else -> {
            }
        }
    }

    private fun setToolbar() {
        setSupportActionBar(binding.homeToolbar)
        binding.homeToolbar.navigationIcon?.setTint(Color.WHITE)
    }

    private fun setClickListeners() {
        binding.fab.apply {
            // make icon white
            setColorFilter(Color.WHITE)
            shapeAppearanceModel =
                shapeAppearanceModel.withCornerSize { BUTTON_ROUND_SIZE }
            // set listeners
            setOnClickListener { openAddItemDialog() }
        }

        binding.bottomAppBar.apply {
            setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_home -> {
                        binding.bottomAppBar.menu.setGroupCheckable(
                            0,
                            true,
                            true
                        )
                        viewModel.navigateToRoot()
                    }
                    R.id.menu_settings -> {
                        navigateToSettings()
                        return@setOnNavigationItemSelectedListener false
                    }
                }
                true
            }
        }
    }

    private fun openAddItemDialog() {
        when (viewModel.pagerPosition.value) {
            1 -> {
                val dialog = viewModel.workspace.value?.workspaceId?.let {
                    ChannelCreateDialogFragment.newInstance(it)
                }
                dialog?.show(supportFragmentManager, DIALOG_CHANNEL_CREATE)
            }
            0 -> {
                val dialog = viewModel.workspace.value?.workspaceId?.let {
                    WorkspaceCreateDialogFragment.newInstance(it)
                }
                dialog?.show(supportFragmentManager, DIALOG_WORKSPACE_CREATE)
            }
            else -> {
            }
        }
    }

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun setObservers() {
        viewModel.workspace.observe(this) {
            if (it == null) return@observe
            if (!isInShareMessageMode) binding.homeToolbar.title = it.name
            if (viewModel.isAtRoot.value == false) binding.homeToolbar.navigationIcon =
                null else binding.homeToolbar.navigationIcon =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_down, null)
        }
        viewModel.pagerPosition.observe(this) {
            if (it == null || it > 1) return@observe
            if (viewModel.hasSwiped)
                animateFab(it)
            else changeFabBackground(it)
        }

        viewModel.eventEdit.observe(this) {
            when (it.getContentIfNotHandled()) {
                ChannelEvent.EDIT_OPEN -> {
                    val vg = window.decorView.rootView as? ViewGroup ?: return@observe
                    applyDim(vg, DIM_AMOUNT)
                }
                ChannelEvent.EDIT_EXIT -> {
                    val vg = window.decorView.rootView as? ViewGroup ?: return@observe
                    clearDim(vg)
                }
                ChannelEvent.UPDATE_FAILED -> {
                    Toast.makeText(
                        this,
                        getString(R.string.error_channel_update_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                }
            }
        }
        viewModel.isAtRoot.observe(this) { atRoot ->
            if (atRoot == true) {
                binding.bottomAppBar.menu.setGroupCheckable(0, true, true)
                try {
                    if (favourite.isAdded) {
                        if (favourite.view?.isVisible == false)
                            favourite.view?.isVisible = true
                        return@observe
                    }
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add(R.id.favourites_container, favourite)

                    }
                } catch (e: Exception) {
                }
            } else if (atRoot == false) {
                favourite.view?.isVisible = false
                binding.bottomAppBar.menu.setGroupCheckable(0, false, true)
            }
        }
        viewModel.pagerScrolled.observe(this) {
            when (it.getContentIfNotHandled()) {
                PagerEvent.PAGER_SCROLLED -> {
                    backdrop_view.closeBackdrop()
                }
                else -> {
                }
            }
        }
        // set observer if no user
        // intent filter used exactly for this activity to avoid blinking while
        // switching from launcher activity to this
        viewModel.eventWorkspace.observe(this) {
            when (val e = it.peekContent()) {
                is WorkspaceEvent.NoUser -> {
                    if (isInShareMessageMode) {
                        startActivity(Intent(this, LauncherActivity::class.java))
                        finishAfterTransition()
                    }
                }
                is WorkspaceEvent.Backdrop -> {
                    if (e.close) binding.backdropView.closeBackdrop()
                    else binding.backdropView.openBackdrop()
                }
                is WorkspaceEvent.UpdateFailed -> {
                    Toast.makeText(
                        this,
                        getString(R.string.error_channel_update_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is WorkspaceEvent.UpdateNeeded -> {
                    val intent = Intent(this, UpdateActivity::class.java)
                    startActivity(intent)
                    finishAfterTransition()
                }
                else -> {
                }
            }
        }
    }

    var favourite: Fragment = FavouriteChannelsFragment.newInstance()


    private fun changeFabBackground(position: Int) {
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val toCircle = position == 1
        val value = if (toCircle) BUTTON_ROUND_SIZE else BUTTON_ROUNDED_SQUARE_SIZE
        fab.apply {
            shapeAppearanceModel =
                shapeAppearanceModel.withCornerSize { (convertDpToPx(value)) }
        }
    }

    private fun applyDim(parent: ViewGroup, dimAmount: Float) {
        val dim: Drawable = ColorDrawable(Color.BLACK)
        dim.setBounds(0, 0, parent.width, parent.height)
        dim.alpha = (255 * dimAmount).toInt()
        val overlay = parent.overlay
        overlay.add(dim)
    }

    private fun clearDim(parent: ViewGroup) {
        val overlay = parent.overlay
        overlay.clear()

    }

    private fun animateFab(position: Int) {
        // if is currently on channels fragment, animate to circle
        val toCircle = position == 1
        val startEnd =
            if (toCircle) BUTTON_ROUNDED_SQUARE_SIZE to BUTTON_ROUND_SIZE else BUTTON_ROUND_SIZE to BUTTON_ROUNDED_SQUARE_SIZE
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        lifecycleScope.launch {
            ObjectAnimator.ofFloat(fab, "interpolation", startEnd.first, startEnd.second).apply {
                duration = ANIMATION_DURATION_FAB
                addUpdateListener { animator ->
                    fab.apply {
                        shapeAppearanceModel =
                            shapeAppearanceModel.withCornerSize { (convertDpToPx(animator.animatedValue as Float)) }
                    }
                }
            }.start()
        }
    }

    override fun onStart() {
        super.onStart()
        reLogInObservable.addObserver(this)
    }

    override fun onStop() {
        super.onStop()
        reLogInObservable.removeObserver(this)
    }

    override fun submit() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finishAfterTransition()
    }


    companion object {
        const val ANIMATION_DURATION_FAB = 300L
        const val DIM_AMOUNT = 0.3f

        // 18f - round dps for square button
        // 45f - for circle button
        const val BUTTON_ROUNDED_SQUARE_SIZE = 18f
        const val BUTTON_ROUND_SIZE = 40f
        const val DIALOG_CHANNEL_CREATE = "DIALOG_CHANNEL_CREATE"
        const val DIALOG_WORKSPACE_CREATE = "DIALOG_WORKSPACE_CREATE"
        const val REQUEST_CODE_CHANNEL_ACTIVITY = 100
        const val BUNDLE_WORKSPACE_EXTRA = "BUNDLE_WORKSPACE_EXTRA"
        const val BUNDLE_CHANNEL_EXTRA = "BUNDLE_CHANNEL_EXTRA"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CHANNEL_ACTIVITY) {
                data?.getBooleanExtra(BUNDLE_WORKSPACE_EXTRA, false)?.let {
                    if (it) viewModel.loadWorkspace(viewModel.workspace.value?.workspaceId)
                }
            }
        }
    }
}
