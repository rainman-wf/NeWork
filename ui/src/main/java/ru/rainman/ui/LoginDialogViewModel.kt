package ru.rainman.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class LoginDialogViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel (){

    val token = userRepository.authToken.asLiveData(viewModelScope.coroutineContext)

    val authError = userRepository.authError.asLiveData(viewModelScope.coroutineContext)

    fun login(login: String, password: String) {
        viewModelScope.launch {
            userRepository.login(login, password)
        }
    }

}