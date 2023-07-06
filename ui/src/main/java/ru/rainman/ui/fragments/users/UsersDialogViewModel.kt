package ru.rainman.ui.fragments.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.model.User
import ru.rainman.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class UsersDialogViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    fun users(list: List<Long>) {
        viewModelScope.launch {
            _users.postValue(userRepository.getByIds(list))
        }
    }
}