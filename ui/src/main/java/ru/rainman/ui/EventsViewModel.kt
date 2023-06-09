package ru.rainman.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.repository.EventRepository
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsRepository: EventRepository
) : ViewModel() {

    val events = eventsRepository.data.cachedIn(viewModelScope)

    fun like(eventId: Long) {
        viewModelScope.launch {
            eventsRepository.like(eventId)
        }
    }

    fun participate(eventId: Long) {
        viewModelScope.launch {
            eventsRepository.participate(eventId)
        }
    }

    fun delete(eventId: Long) {
        viewModelScope.launch {
            eventsRepository.delete(eventId)
        }
    }
}