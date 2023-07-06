package ru.rainman.ui.fragments.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.model.Job
import ru.rainman.domain.model.User
import ru.rainman.domain.repository.JobRepository
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.interact
import ru.rainman.ui.helperutils.states.InteractionResultState
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val repository: UserRepository,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    val interaction = SingleLiveEvent<InteractionResultState>()

    fun getUser(id: Long) {
        viewModelScope.launch {
            _user.postValue(repository.getById(id))
        }
    }

    fun delete(id: Long) {
        interact(interaction) {
            jobRepository.delete(id)
        }
    }

}