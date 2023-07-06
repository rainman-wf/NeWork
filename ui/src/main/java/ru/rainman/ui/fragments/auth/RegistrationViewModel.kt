package ru.rainman.ui.fragments.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.dto.NewUserDto
import ru.rainman.domain.model.ApiError
import ru.rainman.domain.model.AppError
import ru.rainman.domain.model.UploadMedia
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.interact
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.InteractionResultState
import ru.rainman.ui.helperutils.states.Success
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _avatar = MutableLiveData<String?>()
    val avatar: LiveData<String?> get() = _avatar

    val userCreated = SingleLiveEvent<InteractionResultState>()

    fun setAvatar(uri: String) {
        _avatar.postValue(uri)
    }

    fun clearAvatar() {
        _avatar.postValue(null)
    }

    fun create(login: String, password: String, name: String, avatar: UploadMedia?) {
        interact(userCreated) {
            userRepository.create(NewUserDto(login, password, name, avatar))
            userCreated.postValue(Success)
        }
    }
}