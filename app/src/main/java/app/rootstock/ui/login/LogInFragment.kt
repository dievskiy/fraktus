package app.rootstock.ui.login

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import app.rootstock.R
import app.rootstock.data.user.UserWithPassword
import app.rootstock.databinding.FragmentLoginBinding
import app.rootstock.ui.main.WorkspaceActivity
import app.rootstock.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogInFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private val viewModel: LogInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener { view.findNavController().navigateUp() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewmodel = viewModel

        setObservers()

    }

    private fun setObservers() {
        viewModel.logInStatus.observe(viewLifecycleOwner) {
            when (it.peekContent()) {
                EventUserLogIn.SUCCESS -> startMainWorkspaceActivity()
                EventUserLogIn.INVALID_DATA -> makeToast(
                    getString(R.string.invalid_email_or_password),
                    false
                )
                EventUserLogIn.FAILED -> makeToast(
                    getString(R.string.login_failed),
                    false
                )
                EventUserLogIn.LOADING -> {
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        viewModel.stopLogIn()
    }

    private fun startMainWorkspaceActivity() {
        val intent = Intent(requireContext(), WorkspaceActivity::class.java)
        startActivity(intent)
        requireActivity().finishAfterTransition()
    }
}
