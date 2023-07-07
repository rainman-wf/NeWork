package ru.rainman.ui.fragments.users

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.bumptech.glide.Glide
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.common.log
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentUserInfoBinding
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.snack
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.Loading
import ru.rainman.ui.helperutils.states.Success

@AndroidEntryPoint
class UserInfoFragment : Fragment(R.layout.fragment_user_info) {

    private val viewModel: UserInfoViewModel by viewModels()
    lateinit var binding: FragmentUserInfoBinding
    private val args: UserInfoFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val isSettings = args.isSettings

        binding = FragmentUserInfoBinding.bind(view)

        log(isSettings)

        binding.add.isVisible = isSettings

        val navController = requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        viewModel.interaction.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    is Error -> snack(it.message)
                    Loading -> snack("Sending...")
                    Success -> {}
                }
            }
        }

        binding.add.setOnClickListener {
            val dialog = JobEditorDialogFragment()
            val bundle = Bundle()
            bundle.putLong("userId", 0)
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, null)
        }

        viewModel.getUser(args.userId)
        val adapter = if (isSettings) MyJobsAdapter(object : OnMyJobClickListener {
            override fun onDeleteClicked(id: Long) {
                viewModel.delete(id)
            }

            override fun onEditClicked(id: Long) {
                val dialog = JobEditorDialogFragment()
                val bundle = Bundle()
                bundle.putLong("userId", id)
                dialog.arguments = bundle
                dialog.show(parentFragmentManager, null)
            }

        }) else JobListAdapter()

        binding.jobs.adapter = adapter

        binding.jobs.addItemDecoration(MaterialDividerItemDecoration(requireContext(), VERTICAL))

        viewModel.user.observe(viewLifecycleOwner) {

            adapter.submitList(it.jobs)

            binding.title.text = it.name

            it.avatar?.let {url ->
                Glide
                    .with(binding.avatar.context)
                    .load(url)
                    .override(128,128)
                    .placeholder(R.drawable.avatar_stub_large)
                    .error(R.drawable.avatar_error)
                    .circleCrop()
                    .into(binding.avatar)
            }
        }
        binding.back.setOnClickListener { navController.navigateUp() }
    }
}