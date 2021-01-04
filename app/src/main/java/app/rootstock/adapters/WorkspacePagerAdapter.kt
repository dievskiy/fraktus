package app.rootstock.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.rootstock.ui.channels.ChannelsListFragment
import app.rootstock.ui.workspace.WorkspaceListFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi


class WorkspacePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    @ExperimentalCoroutinesApi
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> ChannelsListFragment()
            else -> WorkspaceListFragment()
        }
    }
}