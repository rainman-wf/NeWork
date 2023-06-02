package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.rainman.nework.ui.users.UserListArg
import ru.rainman.ui.databinding.FragmentUsersBinding

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_users) {

    private val viewModel: UsersViewModel by viewModels()

    companion object {
        fun newInstance(arg: UserListArg): UsersFragment {
            val usersFragment = UsersFragment()
            val args = Bundle()
            args.putString("filter", arg.name)
            usersFragment.arguments = args
            return usersFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments ?: throw NullPointerException("args not passed")
        val argValue = args.getString("filter") ?: throw NullPointerException("arg not found")

        val filter = UserListArg.valueOf(argValue)

        val binding = FragmentUsersBinding.bind(view)

        val adapter = UserPagingAdapter()

        binding.users.adapter = adapter

        lifecycleScope.launch {
            viewModel.users.collectLatest {
                lifecycleScope.launch {
                    adapter.submitData(
                        when (filter) {
                            UserListArg.ALL -> it
                            UserListArg.FAVORITE -> it.filter { it.favorite }
                        }
                    )
                }
            }
        }


    }
}