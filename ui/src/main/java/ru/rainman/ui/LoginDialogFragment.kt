package ru.rainman.ui

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.databinding.DialogFragmentLoginBinding
import ru.rainman.ui.helperutils.snack

@AndroidEntryPoint
class LoginDialogFragment : DialogFragment(R.layout.dialog_fragment_login) {

    private lateinit var binding: DialogFragmentLoginBinding
    private val viewModel: LoginDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = DialogFragmentLoginBinding.inflate(requireActivity().layoutInflater)

        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setView(binding.root)
        builder.setTitle("Input auth data")

        binding.buttonPositive.setOnClickListener {

            val login = binding.loginInputLogin.text
            val invalidLogin = login.isNullOrBlank()

            val password = binding.loginInputPassword.text
            val invalidPassword = password.isNullOrBlank()



            when  {
                invalidPassword && invalidLogin -> {
                    binding.loginInputLoginLayout.error = "Required field"
                    binding.loginInputPasswordLayout.error = "Required field"
                    binding.loginInputLogin.requestFocus()
                }
                invalidLogin -> {
                    binding.loginInputLoginLayout.error = "Required field"
                    binding.loginInputLogin.requestFocus()
                }
                invalidPassword -> {
                    binding.loginInputPasswordLayout.error = "Required field"
                    binding.loginInputPassword.requestFocus()
                }
                else -> viewModel.login(login.toString(), password.toString())
            }
        }

        binding.buttonNegative.setOnClickListener {
            dismiss()
        }

        binding.loginInputLogin.addTextChangedListener (object  : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) binding.loginInputLoginLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.loginInputPassword.addTextChangedListener (object  : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) binding.loginInputPasswordLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        return builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.token.observe(viewLifecycleOwner) {
            if (it != null) dismiss()
        }

        viewModel.authError.observe(viewLifecycleOwner) {
            Snackbar.make(requireParentFragment().requireView(), it.message, Snackbar.LENGTH_SHORT).show()
        }
    }
}