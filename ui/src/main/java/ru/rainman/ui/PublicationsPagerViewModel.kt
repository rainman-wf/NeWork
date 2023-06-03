package ru.rainman.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.CurrentPlayedItemState
import ru.rainman.ui.helperutils.PubType
import javax.inject.Inject

@HiltViewModel
class PublicationsPagerViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val isLoggedIn = userRepository.authToken.map { it != null }.asLiveData(viewModelScope.coroutineContext)

    private val _currentPlayedItemState = MutableLiveData<CurrentPlayedItemState?>(null)
    val currentPlayedItemState : LiveData<CurrentPlayedItemState?> get() = _currentPlayedItemState

    fun setCurrentPlayedItem(type: PubType, pubId: Long) {
        _currentPlayedItemState.postValue(CurrentPlayedItemState(type, pubId, false))
    }

    fun clearCurrentPlayedItem() {
        _currentPlayedItemState.postValue(null)
    }

    fun setIsPlaying(value: Boolean) {
        _currentPlayedItemState.value?.let {
            _currentPlayedItemState.postValue(it.copy(isPlaying = value))
        }
    }
}

