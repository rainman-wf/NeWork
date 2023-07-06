package ru.rainman.ui.fragments.publications.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.repository.EventRepository
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.interact
import ru.rainman.ui.helperutils.states.InteractionResultState
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsRepository: EventRepository
) : ViewModel() {

    val events = eventsRepository.data.cachedIn(viewModelScope)

    val interaction  = SingleLiveEvent<InteractionResultState>()

    fun like(eventId: Long) {
        interact(interaction) {
            eventsRepository.like(eventId)
        }
    }

    fun participate(eventId: Long) {
        interact(interaction) {
            eventsRepository.participate(eventId)
        }
    }

    fun delete(eventId: Long) {
        interact(interaction) {
            eventsRepository.delete(eventId)
        }
    }
}