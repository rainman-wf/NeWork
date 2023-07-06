package ru.rainman.ui.fragments.users

import android.app.Dialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.R
import ru.rainman.ui.databinding.DialogFragmentSelectUsersBinding
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.args.ArgKey
import ru.rainman.ui.helperutils.args.RequestKey
import ru.rainman.ui.helperutils.args.putResult
import ru.rainman.ui.helperutils.menuItemHandle


@AndroidEntryPoint
class SelectUsersDialogFragment : DialogFragment(R.layout.dialog_fragment_select_users) {

    private lateinit var binding: DialogFragmentSelectUsersBinding
    private val viewModel: SelectUsersDialogViewModel by viewModels()
    private val args: SelectUsersDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogFragmentSelectUsersBinding.inflate(requireActivity().layoutInflater)
        val builder = MaterialAlertDialogBuilder(requireContext())

        setStyle(STYLE_NORMAL, R.style.FullscreenDialog)

        builder.setView(binding.root)

        binding.buttonPositive.setOnClickListener {
            val bundle = Bundle()
            bundle.putLongArray(
                ArgKey.USERS.name,
                viewModel.selectedIds.toLongArray()
            )
            when (args.editableType) {
                PubType.POST ->
                    putResult(RequestKey.POST_REQUEST_KEY_MENTIONED, bundle)
                PubType.EVENT ->
                    putResult(RequestKey.EVENT_REQUEST_KEY_SPEAKERS, bundle)
            }
            dismiss()
        }

        binding.buttonNegative.setOnClickListener {
            val bundle = Bundle()
            bundle.putLongArray(ArgKey.USERS.name, args.ids)

            when(args.editableType) {
                PubType.EVENT ->
                    setFragmentResult(RequestKey.EVENT_REQUEST_KEY_SPEAKERS.name, bundle)
                PubType.POST ->
                    setFragmentResult(RequestKey.POST_REQUEST_KEY_MENTIONED.name, bundle)
            }
            dismiss()
        }

        return builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.selectUsers(args.ids.toSet())

        binding.filterUserByName.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setName(newText)
                return true
            }
        })

        binding.userSelectToolbar.overflowIcon =
            AppCompatResources.getDrawable(requireContext(), R.drawable.filter)
        binding.userSelectToolbar.menu.setGroupDividerEnabled(true)

        viewModel.filterState.observe(viewLifecycleOwner) {

            binding.userSelectToolbar.menu.apply {
                getItem(
                    when (it.selected) {
                        null -> 0
                        true -> 1
                        false -> 2
                    }
                ).isChecked = true
            }
        }

        binding.userSelectToolbar.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.select_all -> menuItemHandle { viewModel.setSelected(null) }
                R.id.select_selected -> menuItemHandle { viewModel.setSelected(true) }
                R.id.select_unselected -> menuItemHandle { viewModel.setSelected(false) }
                else -> false
            }
        }

        binding.userSelectToolbar.menu.children.forEach {
            it.actionView = binding.root
            it.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    return false
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    return false
                }
            })
        }

        val adapter = SelectableUserAdapter {
            viewModel.selectUser(it.user.id)
        }

        binding.selectableUsers.adapter = adapter

        viewModel.selectableUsers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}



