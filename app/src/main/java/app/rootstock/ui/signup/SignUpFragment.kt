package app.rootstock.ui.signup

import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import app.rootstock.databinding.FragmentSignupBinding
import app.rootstock.di.modules.AppModule
import app.rootstock.ui.main.WorkspaceActivity
import app.rootstock.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener { view.findNavController().navigateUp() }
        toolbar.navigationIcon?.setTint(Color.WHITE)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewmodel = viewModel

        setObservers()

        val txtPrivacy = view?.findViewById<TextView>(R.id.privacy_policy)
        txtPrivacy?.let { setUpTextPrivacy(it) }
    }

    private fun setUpTextPrivacy(txtPrivacy: TextView) {
        txtPrivacy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppModule.URL_PRIVACY_POLICY))
            startActivity(intent)
        }
        try {
            val spannable = SpannableString(txtPrivacy.text ?: getString(R.string.sign_up_privacy))
            spannable.setSpan(
                ForegroundColorSpan(requireContext().getColor(R.color.primary)),
                28,
                42,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            txtPrivacy.text = spannable
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    private fun setObservers() {
        viewModel.signUpStatus.observe(viewLifecycleOwner) {
            when (it.getContentIfNotHandled()) {
                EventUserSignUp.SUCCESS -> startMainWorkspaceActivity()
                EventUserSignUp.USER_EXISTS -> makeToast(
                    getString(R.string.invalid_email),
                    long = true
                )
                EventUserSignUp.INVALID_DATA -> makeToast(getString(R.string.invalid), false)
                EventUserSignUp.FAILED -> makeToast(
                    getString(R.string.signup_failed),
                    false
                )
                EventUserSignUp.LOADING -> {
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopSignUp()
    }

    private fun startMainWorkspaceActivity() {
        val intent = Intent(requireContext(), WorkspaceActivity::class.java)
        startActivity(intent)
        requireActivity().finishAfterTransition()
    }
}

