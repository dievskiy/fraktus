package app.rootstock.ui.workspace

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.rootstock.R
import app.rootstock.adapters.WorkspacePagerAdapter
import app.rootstock.databinding.FragmentWorkspaceBinding
import app.rootstock.ui.main.WorkspaceEvent
import app.rootstock.ui.main.WorkspaceViewModel
import app.rootstock.utils.isTablet
import app.rootstock.utils.makeToast
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class WorkspaceFragment : Fragment() {

    private val args: WorkspaceFragmentArgs by navArgs()

    private val viewModel: WorkspaceViewModel by activityViewModels()

    private lateinit var binding: FragmentWorkspaceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWorkspaceBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    private val changeFab = object : ViewPager2.OnPageChangeCallback() {
        var currentPosition: Int? = 0
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (currentPosition != position) {
                currentPosition = position
                viewModel.changeFab(position)
            }
        }

    }

    private var indicatorWidth: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup adapter for viewpager with tablayout
        val adapterToSet = WorkspacePagerAdapter(this)
        binding.pager.apply {
            // preload also channel fragment
            offscreenPageLimit = 2;
            adapter = adapterToSet
            registerOnPageChangeCallback(changeFab)
            // disable overscroll effect
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = when (position) {
                1 -> "channels"
                else -> "workspaces"
            }
        }.attach()

        //Determine indicator width at runtime
        binding.tabLayout.post {
            indicatorWidth = binding.tabLayout.width / binding.tabLayout.tabCount

            //Assign new width
            val indicatorParams = binding.indicator.layoutParams as? FrameLayout.LayoutParams
            indicatorWidth?.let {
                indicatorParams?.width = it
                binding.indicator.layoutParams = indicatorParams
            }
        }

        if (requireContext().isTablet()) {
            binding.indicator.isVisible = false

        } else {
            binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(i: Int, positionOffset: Float, positionOffsetPx: Int) {
                    indicatorWidth ?: return
                    val params = binding.indicator.layoutParams as FrameLayout.LayoutParams

                    val translationOffset: Float = (positionOffset + i) * indicatorWidth!!
                    params.leftMargin = translationOffset.toInt()
                    binding.indicator.layoutParams = params
                }

                override fun onPageSelected(i: Int) {}
                override fun onPageScrollStateChanged(i: Int) {}
            })
        }

        viewModel.loadWorkspace(args.workspaceId)
        viewModel.setRoot(isAtRoot())
        setObservers()
        viewModel.resetPager()
    }

    private fun setObservers() {
        viewModel.eventWorkspace.observe(viewLifecycleOwner) {
            when (val content = it.getContentIfNotHandled()) {
                is WorkspaceEvent.OpenWorkspace -> {
                    openWorkspace(content.workspaceId)
                }
                is WorkspaceEvent.NoUser -> {
                    makeToast(getString(R.string.error_workspace_no_user))
                }
                is WorkspaceEvent.Error -> {

                }
                is WorkspaceEvent.NavigateToRoot -> {
                    navigateToRoot()
                }
                null -> {
                }
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!isAtRoot())
                        findNavController().navigateUp()
                    else activity?.finish()
                }
            })
    }

    private fun isAtRoot() = args.workspaceId == null

    private fun navigateToRoot() {
        if (!isAtRoot()) {
            findNavController().navigate(R.id.workspace_fragment)
        }
    }

    /**
     * navigates to workspace using workspaceId
     * If workspaceId is null, we are in a root workspace
     */
    private fun openWorkspace(workspaceId: String) {
        findNavController().navigate(
            WorkspaceFragmentDirections.actionWorkspaceFragmentSelf(
                workspaceId
            )
        )
    }


}