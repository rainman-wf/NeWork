package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.databinding.FragmentPostsBinding
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.activityFragmentManager
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.showVideoDialog
import ru.rainman.ui.helperutils.snack

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {

    private val binding: FragmentPostsBinding by viewBinding(FragmentPostsBinding::bind)
    private lateinit var navController: NavController
    private val viewModel: PostsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController =
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        val parentFragment = requireParentFragment() as PagerFragment

        val adapter = PostListAdapter(parentFragment.currentPlayedItem, object : OnPostClickListener {
            override fun onLikeClicked(id: Long) {
                viewModel.like(id)
            }

            override fun onShareClicked(id: Long) {
                snack("share $id")
            }

            override fun onEditClicked(id: Long) {
               activityFragmentManager().getNavController(R.id.out_of_main_nav_host).navigate(MainFragmentDirections.actionMainFragmentToPostEditorFragment(id))
            }

            override fun onDeleteClicked(id: Long) {
                viewModel.delete(id)
            }

            override fun onAuthorClicked(id: Long) {
                snack("author $id")
            }

            override fun onBodyClicked(id: Long) {
                snack("post $id")
            }

            override fun onPlayClicked(id: Long, attachment: Attachment) {
                when (attachment) {
                    is Attachment.Video -> {
                        parentFragment.stopAudio()
                        showVideoDialog(attachment.uri, attachment.ratio)
                    }
                    is Attachment.Audio -> parentFragment.playAudio(attachment.uri, PubType.POST, id)
                    else -> {}
                }
            }
        })

        binding.postList.adapter = adapter

        binding.postList.itemAnimator = null

        lifecycleScope.launch {
            viewModel.posts.collectLatest {
                adapter.submitData(it)
            }
        }
    }


}