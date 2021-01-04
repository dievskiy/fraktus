package app.rootstock.ui.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.rootstock.R
import app.rootstock.databinding.FragmentAccountStartBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class IntroFragment : Fragment() {

    private lateinit var binding: FragmentAccountStartBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountStartBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val signUp = view.findViewById<View>(R.id.signup)
        val logIn = view.findViewById<View>(R.id.login)

        signUp.setOnClickListener {
            navigateToSignUp()
        }

        logIn.setOnClickListener {
            navigateToLogIn()
        }

    }

    private fun navigateToLogIn() {
        val action = IntroFragmentDirections.actionIntroFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    private fun navigateToSignUp() {
        val action = IntroFragmentDirections.actionIntroFragmentToSigninFragment()
        findNavController().navigate(action)

    }

}