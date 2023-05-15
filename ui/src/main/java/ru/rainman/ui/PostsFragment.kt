package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.databinding.FragmentPostsBinding

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {

    private val binding: FragmentPostsBinding by viewBinding(FragmentPostsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }
}