package ru.rainman.ui.fragments.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentRegistrationBinding
import ru.rainman.ui.fragments.SelectAvatarBottomSheetDialog
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.snack
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.Loading
import ru.rainman.ui.helperutils.states.Success
import ru.rainman.ui.helperutils.toUploadMedia
import kotlin.properties.Delegates

@AndroidEntryPoint
class RegistrationFragment : Fragment(R.layout.fragment_registration) {

    private val viewModel: RegistrationViewModel by viewModels()
    private lateinit var binding: FragmentRegistrationBinding

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            SelectAvatarBottomSheetDialog().show(parentFragmentManager, "AVATAR")
        }

    private var storagePermissionGranted by Delegates.notNull<Boolean>()

    private fun requestPermission() {
        if (!storagePermissionGranted)
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storagePermissionGranted = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = FragmentRegistrationBinding.bind(view)

        binding.registrationPickImage.setOnClickListener {
            if (storagePermissionGranted)
                SelectAvatarBottomSheetDialog().show(parentFragmentManager, "AVATAR")
            else requestPermission()
        }

        setFragmentResultListener("REGISTRATION_AVATAR") { _, bundle ->
            val uri = bundle.getString("uri")
            uri?.let {
                viewModel.setAvatar(it)
            }
        }

        binding.registrationToolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)
                .navigateUp()
        }

        viewModel.avatar.observe(viewLifecycleOwner) {
            binding.registrationClearAvatar.isVisible = it != null
            Glide.with(requireContext())
                .load(it)
                .placeholder(R.drawable.avatar_stub_large)
                .circleCrop()
                .into(binding.avatar)
        }

        binding.registrationClearAvatar.setOnClickListener {
            viewModel.clearAvatar()
        }

        binding.done.setOnClickListener {
            val login = binding.inputLogin.text
            val invalidLogin = login.isNullOrBlank()

            val password = binding.inputPassword.text
            val invalidPassword = password.isNullOrBlank()

            val name = binding.inputName.text
            val invalidName = name.isNullOrBlank()


            when {
                invalidLogin -> {
                    binding.inputLoginLayout.error = "Required field"
                    binding.inputLogin.requestFocus()
                }

                invalidPassword -> {
                    binding.inputPasswordLayout.error = "Required field"
                    binding.inputPassword.requestFocus()
                }

                invalidName -> {
                    binding.inputNameLayout.error = "Required field"
                    binding.inputName.requestFocus()
                }

                else -> viewModel.create(
                    login.toString(),
                    password.toString(),
                    name.toString(),
                    viewModel.avatar.value?.toUri()?.toUploadMedia(requireContext())
                )
            }
        }

        viewModel.userCreated.observe(viewLifecycleOwner) {
            when (it) {
                is Error -> snack(it.message)
                Loading -> {}
                Success -> requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)
                    .navigateUp()
                null -> {}
            }

        }

        binding.inputLogin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) binding.inputLoginLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) binding.inputPasswordLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.inputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) binding.inputNameLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}

        })

    }
}