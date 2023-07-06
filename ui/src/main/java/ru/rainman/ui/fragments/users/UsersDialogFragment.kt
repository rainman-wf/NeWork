package ru.rainman.ui.fragments.users

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.R
import ru.rainman.ui.databinding.DialogFragmentUsersBinding

@AndroidEntryPoint
class UsersDialogFragment : DialogFragment(R.layout.dialog_fragment_users) {

    lateinit var binding: DialogFragmentUsersBinding
    private val viewModel: UsersDialogViewModel by viewModels()
    private lateinit var args: Bundle

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogFragmentUsersBinding.inflate(requireActivity().layoutInflater)
        args = arguments ?: throw NullPointerException("${this::class.simpleName} : args missed")

        val builder = MaterialAlertDialogBuilder(requireContext())

        builder.apply {
            setView(binding.root)
            setTitle(args.getString("intention"))
            setNegativeButton("Close") { _, _->
                this@UsersDialogFragment.dismiss()
            }
        }

        return builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SimpleUserListAdapter()
        binding.root.adapter = adapter
        viewModel.users(args.getLongArray("ids")?.toList() ?: emptyList())
        viewModel.users.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

    }
}