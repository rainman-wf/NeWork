package ru.rainman.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.rainman.ui.helperutils.CurrentPlayedItemState
import ru.rainman.ui.helperutils.PubType
import javax.inject.Inject

@HiltViewModel
class PublicationsPagerViewModel @Inject constructor() : ViewModel() {

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

