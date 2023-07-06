package ru.rainman.ui.fragments.publications.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.rainman.common.log
import ru.rainman.domain.model.Post
import ru.rainman.domain.model.User
import ru.rainman.domain.repository.PostRepository
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.interact
import ru.rainman.ui.helperutils.states.InteractionResultState
import javax.inject.Inject


@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postsRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val token = userRepository.authToken.asLiveData(viewModelScope.coroutineContext)
    val interaction = SingleLiveEvent<InteractionResultState>()
    val posts = postsRepository.data.cachedIn(viewModelScope)

    fun wall(ownerId: Long) = postsRepository.wall(
        if (ownerId == 0L) token.value?.id!! else ownerId
    ).cachedIn(viewModelScope)

    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> get() = _user

    fun like(postId: Long) {
        log(postId)
        interact(interaction) {
            postsRepository.like(postId)
        }
    }

    fun delete(postId: Long) {
        interact(interaction) {
            postsRepository.delete(postId)
        }
    }

    fun setCurrentUser(userId: Long) {
        viewModelScope.launch {
            val ownerId =
                if (userId == 0L) token.value?.id ?: throw RuntimeException("U R not authorized")
                else userId
            _user.postValue(userRepository.getById(ownerId))
        }
    }
}