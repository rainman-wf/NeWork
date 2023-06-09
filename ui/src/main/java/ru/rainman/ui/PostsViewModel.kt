package ru.rainman.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.rainman.domain.repository.PostRepository
import javax.inject.Inject


@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postsRepository: PostRepository
) : ViewModel () {

    val posts = postsRepository.data.cachedIn(viewModelScope)

    fun like(postId: Long) {
        viewModelScope.launch {
            postsRepository.like(postId)
        }
    }

    fun delete(postId: Long) {
        viewModelScope.launch {
            postsRepository.delete(postId)
        }
    }
}