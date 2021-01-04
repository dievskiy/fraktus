package app.rootstock.views

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import app.rootstock.databinding.DialogDeleteAccountBinding
import app.rootstock.ui.settings.SettingsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class DeleteAccountDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val ARGUMENT_EMAIL = "ARGUMENT_EMAIL"

        fun newInstance(email: String): DeleteAccountDialogFragment {
            return DeleteAccountDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGUMENT_EMAIL, email)
                }
            }
        }
    }

    private lateinit var binding: DialogDeleteAccountBinding

    private var email: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogDeleteAccountBinding.inflate(layoutInflater, container, true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        email = arguments?.getString(ARGUMENT_EMAIL)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
        }

        binding.deleteButton.setOnClickListener {
            if (binding.email.editText?.text.toString() == email) {
                val viewModel: SettingsViewModel by activityViewModels()
                val mail = binding.email.editText?.text.toString()
                viewModel.deleteAccount(mail)
                dismiss()
            } else {
                binding.email.error = "Invalid email"
            }
        }
        binding.cancel.setOnClickListener { dismiss() }

        if (showsDialog) {
            (requireDialog() as AlertDialog).setView(binding.root)
        }
    }


}

