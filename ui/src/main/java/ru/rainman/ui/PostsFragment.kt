package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.rainman.ui.databinding.FragmentPostsBinding
import ru.rainman.ui.helperutils.snack

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {

    private val binding: FragmentPostsBinding by viewBinding(FragmentPostsBinding::bind)
    private val viewModel: PostsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val adapter = PostsAdapter(object : OnPostClickListener {
            override fun onLikeClicked(postId: Long) {
                snack("like $postId")
            }

            override fun onShareClicked(postId: Long) {
                snack("share $postId")
            }

            override fun onMoreClicked(postId: Long) {
                snack("more $postId")
            }

            override fun onAuthorClicked(postId: Long) {
                snack("author $postId")
            }

            override fun onPostClicked(postId: Long) {
                snack("post $postId")
            }

        })

        binding.postList.adapter = adapter

        lifecycleScope.launch {
            viewModel.posts.collectLatest {
                adapter.submitData(it)
            }
        }
    }
}