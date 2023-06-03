package ru.rainman.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.dto.NewEventDto
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.User
import ru.rainman.domain.repository.ApiTestRepository
import ru.rainman.domain.repository.EventRepository
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.SimpleLocation
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.TimeUnitsWrapper
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EventEditorViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) : ViewModel(){

    private val _isOnline = MutableLiveData(true)
    val isOnline: LiveData<Boolean> get () = _isOnline

    enum class PublishingState {
        LOADING,
        ERROR,
        SUCCESS
    }

    private val _date = MutableLiveData<Long?>()
    val date: LiveData<Long?> get () = _date

    val eventStatus = SingleLiveEvent<PublishingState>()

    private val _time = MutableLiveData<TimeUnitsWrapper?>()
    val time: LiveData<TimeUnitsWrapper?> get () = _time

    private val _speakers = MutableLiveData<List<User>>(emptyList())
    val speakers: LiveData<List<User>> get() = _speakers

    private val _location = MutableLiveData<SimpleLocation?>(null)
    val location: LiveData<SimpleLocation?> get() = _location

    private val _attachment = MutableLiveData<Attachment?>()
    val attachment: LiveData<Attachment?> get() = _attachment

    fun online (value: Boolean) = _isOnline.postValue(value)
    fun setDate(value: Long?) = _date.postValue(value)
    fun setTime(value: TimeUnitsWrapper?) = _time.postValue(value)

    fun setSpeakers(ids: List<Long>) {
        viewModelScope.launch {
            _speakers.postValue(userRepository.getByIds(ids))
        }
    }


    fun publish(newEventDto: NewEventDto) {
        viewModelScope.launch {
            eventStatus.postValue(PublishingState.LOADING)
            val event = eventRepository.create(newEventDto)
            if (event == null) {
                eventStatus.postValue(PublishingState.ERROR)
            } else {
                eventStatus.postValue(PublishingState.SUCCESS)
            }

        }
    }

    fun setLocation(simpleLocation: SimpleLocation?) {
        _location.postValue(simpleLocation)
    }

    fun setAttachment(attachment: Attachment?) {
        _attachment.postValue(attachment)
    }

    fun removeSpeaker(userId: Long ) {
        val old = _speakers.value?.toMutableList() ?: mutableListOf()
        val new = old.filterNot { it.id == userId }
        _speakers.postValue(new)
    }

}