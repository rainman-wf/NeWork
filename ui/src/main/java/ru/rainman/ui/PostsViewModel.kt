package ru.rainman.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.rainman.domain.repository.PostRepository
import javax.inject.Inject


@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postsRepository: PostRepository
) : ViewModel () {

    val posts = postsRepository.data.cachedIn(viewModelScope)
}