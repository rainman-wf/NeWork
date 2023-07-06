package ru.rainman.ui.fragments.publications.post

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentPostsBinding
import ru.rainman.ui.fragments.MainFragmentDirections
import ru.rainman.ui.fragments.users.UsersDialogFragment
import ru.rainman.ui.helperutils.PlayerHolder
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.activityFragmentManager
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.setIcon
import ru.rainman.ui.helperutils.showUsersDialog
import ru.rainman.ui.helperutils.showVideoDialog
import ru.rainman.ui.helperutils.snack
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.Loading
import ru.rainman.ui.helperutils.states.Success

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {

    companion object {
        fun newInstance(arg: Long): PostsFragment {
            val postsFragment = PostsFragment()
            val args = Bundle()
            args.putLong("user_id", arg)
            postsFragment.arguments = args
            return postsFragment
        }
    }

    private val binding: FragmentPostsBinding by viewBinding(FragmentPostsBinding::bind)
    private lateinit var navController: NavController
    private val viewModel: PostsViewModel by viewModels()
    private val args: PostsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val arg = args.userId

        binding.wallAppBar.isVisible = arg > 0

        if (arg >= 0) viewModel.setCurrentUser(arg)

        navController = activityFragmentManager().getNavController(R.id.out_of_main_nav_host)

        viewModel.interaction.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    is Error -> snack(it.message)
                    Loading -> {}
                    Success -> {}
                }
            }
        }

        viewModel.user.observe(viewLifecycleOwner) {
            it?.apply {
                binding.wallToolBar.let { bar ->

                    bar.title = name
                    bar.subtitle = currentJob?.name
                    bar.setNavigationOnClickListener {
                        navController.navigateUp()
                    }

                    bar.menu.getItem(0).setIcon(requireContext(), avatar)
                    bar.menu.getItem(0).setOnMenuItemClickListener {
                        navController
                            .navigate(
                                PostsFragmentDirections.actionPostsFragmentToUserInfoFragment(
                                    id
                                )
                            )
                        true
                    }
                }
            }
        }

        val adapter = PostListAdapter(arg != -1L, PlayerHolder.currentPlayedItem, object :
            OnPostClickListener {
            override fun onMentionedClicked(ids: List<Long>) {
                showUsersDialog("Mentioned users", ids)
            }

            override fun onLikeClicked(id: Long) {
                viewModel.like(id)
            }

            override fun onLikesCountClicked(ids: List<Long>) {
                showUsersDialog("Like owners", ids)
            }

            override fun onShareClicked(id: Long) {
                snack("share $id")
            }

            override fun onEditClicked(id: Long) {
                navController.navigate(
                    MainFragmentDirections.actionMainFragmentToPostEditorFragment(
                        id
                    )
                )
            }

            override fun onDeleteClicked(id: Long) {
                viewModel.delete(id)
            }

            override fun onAuthorClicked(id: Long) {
                if (arg == -1L) navController.navigate(MainFragmentDirections.actionMainFragmentToPostsFragment(id))
            }

            override fun onBodyClicked(id: Long) {
                snack("post $id")
            }

            override fun onPlayClicked(id: Long, attachment: Attachment) {
                when (attachment) {
                    is Attachment.Video -> {
                        PlayerHolder.stopAudio()
                        showVideoDialog(attachment.uri, attachment.ratio)
                    }

                    is Attachment.Audio -> PlayerHolder.playAudio(attachment.uri, PubType.POST, id)
                    else -> {}
                }
            }
        })

        binding.postList.adapter = adapter

        binding.postList.itemAnimator = null

        lifecycleScope.launch {
            when (arg) {
                -1L -> viewModel.posts.collectLatest {
                    adapter.submitData(it)
                }
                else -> viewModel.wall(arg).collectLatest { data ->
                    adapter.submitData(data)
                }
            }
        }
    }
}