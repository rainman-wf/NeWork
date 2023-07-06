package ru.rainman.ui.fragments.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.model.AppError
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.interact
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.InteractionResultState
import javax.inject.Inject

@HiltViewModel
class LoginDialogViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val token = userRepository.authToken.asLiveData(viewModelScope.coroutineContext)

    val loggedIn = SingleLiveEvent<InteractionResultState>()

    fun login(login: String, password: String) {
        interact(loggedIn) {
            userRepository.login(login, password)
        }
    }
}


