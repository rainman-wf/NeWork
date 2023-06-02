package ru.rainman.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.common_utils.log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.rainman.domain.model.Token
import ru.rainman.domain.model.User
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.Me
import javax.inject.Inject

@HiltViewModel
class MainFragmentViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _token = userRepository.authToken
    private val _me = MutableLiveData<Me?>()
    val me: LiveData<Me?> get() = _me

    init {
        viewModelScope.launch {
            _token.collectLatest {
                it?.apply {
                    val user = userRepository.getById(it.id)!!
                    _me.postValue(Me(this, user))
                } ?: _me.postValue(null)
            }
        }
    }

    fun logOut() {
        viewModelScope.launch { userRepository.logOut() }
    }
}


