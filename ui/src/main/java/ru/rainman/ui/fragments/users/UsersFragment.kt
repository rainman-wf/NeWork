package ru.rainman.ui.fragments.users

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentUsersBinding
import ru.rainman.ui.fragments.MainFragmentDirections
import ru.rainman.ui.helperutils.activityFragmentManager
import ru.rainman.ui.helperutils.getNavController

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_users) {

    private val viewModel: UsersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentUsersBinding.bind(view)

        val adapter = UserPagingAdapter {
            activityFragmentManager().getNavController(R.id.out_of_main_nav_host)
                .navigate(MainFragmentDirections.actionMainFragmentToPostsFragment(it))
        }

        binding.users.adapter = adapter

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)

        divider.dividerInsetStart = 64
        divider.dividerInsetEnd = 8

        binding.users.addItemDecoration(divider)

        lifecycleScope.launch {
            viewModel.users.collectLatest {
                lifecycleScope.launch {
                    adapter.submitData(it)
                }
            }
        }
    }
}