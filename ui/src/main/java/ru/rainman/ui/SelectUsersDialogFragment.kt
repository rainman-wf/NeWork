package ru.rainman.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.common_utils.log
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.databinding.DialogFragmentSelectUsersBinding
import ru.rainman.ui.helperutils.menuItemHandle

@AndroidEntryPoint
class SelectUsersDialogFragment : DialogFragment(R.layout.dialog_fragment_select_users) {

    private lateinit var binding: DialogFragmentSelectUsersBinding
    private val viewModel: SelectUsersDialogViewModel by viewModels()
    private val args: SelectUsersDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogFragmentSelectUsersBinding.inflate(requireActivity().layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
            .setPositiveButton("Done") { _, _ ->
                val bundle = Bundle()
                bundle.putLongArray("ids", viewModel.selectedIds.toLongArray())
                setFragmentResult("select_users", bundle)
            }

            .setNegativeButton("Cansel") { _, _ ->
                val bundle = Bundle()
                bundle.putLongArray("ids", args.ids)
                setFragmentResult("select_users", bundle)
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

        viewModel.filterState.observe(viewLifecycleOwner) {

            binding.userSelectToolbar.menu.apply {
                getItem(0).isChecked = it.favorite
                getItem(
                    when (it.selected) {
                        null -> 1
                        true -> 2
                        false -> 3
                    }
                ).isChecked = true
            }
        }

        binding.userSelectToolbar.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.select_users_favorite -> menuItemHandle { viewModel.setFavorite() }
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



