package ru.rainman.ui.fragments.publications.other

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.dto.NewAttachmentDto
import ru.rainman.domain.dto.NewEventDto
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates
import ru.rainman.domain.model.Event
import ru.rainman.domain.model.EventType
import ru.rainman.domain.model.RemoteMedia
import ru.rainman.domain.repository.EventRepository
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.EditableEvent
import ru.rainman.ui.helperutils.SimpleLocation
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.interact
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.InteractionResultState
import ru.rainman.ui.helperutils.toUploadMedia
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class EventEditorViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    val eventStatus = SingleLiveEvent<InteractionResultState>()

    private val _editableEvent = MutableLiveData(EditableEvent(dateTime = LocalDateTime.now()))
    val editableEvent: LiveData<EditableEvent> get() = _editableEvent

    private val _locationName = MutableLiveData<String?>()
    val locationName: LiveData<String?> get() = _locationName

    val oldContent = SingleLiveEvent<String>()

    private var isNewAttachment: Boolean = false

    fun online(value: Boolean) {
        viewModelScope.launch {
            _editableEvent.postValue(
                _editableEvent.value!!.copy(
                    type = if (value) EventType.ONLINE else EventType.OFFLINE
                )
            )
        }
    }

    fun setDate(value: Long) {
        _editableEvent.postValue(
            _editableEvent.value!!
                .copy(
                    dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(value),
                        TimeZone.getDefault().toZoneId()
                    ).toLocalDate().atTime(_editableEvent.value!!.dateTime.toLocalTime())
                )
        )
    }

    fun setTime(hours: Int, minutes: Int) {
        _editableEvent.postValue(
            _editableEvent.value!!
                .copy(
                    dateTime =
                    _editableEvent.value!!.dateTime
                        .toLocalDate().atTime(LocalTime.of(hours, minutes))
                )
        )
    }

    fun setSpeakers(ids: List<Long>) {
        viewModelScope.launch {
            _editableEvent.postValue(
                _editableEvent.value!!.copy(
                    speakers = userRepository.getByIds(
                        ids
                    )
                )
            )
        }
    }

    fun publish(content: String, context: Context) {

        val editable = _editableEvent.value!!

        val isOnline = editable.type == EventType.ONLINE

        editable.apply {

            if (when {
                    dateTime.isBefore(LocalDateTime.now()) ->
                        setError("Date must be after the present time")

                    content.isBlank() ->
                        setError("Content is required")

                    isOnline && link == null ->
                        setError("Link is required for ONLINE mode")

                    !isOnline && coordinates == null ->
                        setError("Location is required for OFFLINE mode")

                    else -> false
                }
            ) return
        }

        val newEventDto = editable.let {
            NewEventDto(
                id = it.id,
                content = content,
                dateTime = it.dateTime,
                speakerIds = it.speakers.map { user -> user.id },
                coordinates = if (!isOnline) it.coordinates else null,
                link = if (isOnline) it.link.toString() else null,
                type = it.type,
                attachment = it.attachment?.let { att ->
                    NewAttachmentDto(
                        type = when (att) {
                            is Attachment.Image -> NewAttachmentDto.Type.IMAGE
                            is Attachment.Video -> NewAttachmentDto.Type.VIDEO
                            is Attachment.Audio -> NewAttachmentDto.Type.AUDIO
                        },
                        media = if (isNewAttachment) att.uri.toUri()
                            .toUploadMedia(context) else RemoteMedia(att.uri)
                    )
                }
            )
        }

        interact(eventStatus) {
            eventRepository.create(newEventDto)
        }
    }

    private fun setError(message: String): Boolean {
        eventStatus.postValue(Error(message))
        return true
    }

    fun setLocation(simpleLocation: SimpleLocation?) {
        _locationName.postValue(simpleLocation?.name)
        _editableEvent.postValue(_editableEvent.value!!.copy(coordinates = simpleLocation?.point?.let {
            Coordinates(
                it.latitude,
                it.longitude
            )
        }))
    }

    fun setAttachment(attachment: Attachment?) {
        val post = _editableEvent.value!!.copy(attachment = attachment)
        _editableEvent.postValue(post)
        isNewAttachment = true
    }

    fun removeSpeaker(userId: Long) {
        val old = _editableEvent.value!!.speakers
        val new = old.filterNot { it.id == userId }
        val post = _editableEvent.value!!.copy(speakers = new)
        _editableEvent.postValue(post)
    }

    private fun Event.toEditable() : EditableEvent {
        oldContent.postValue(content)
        return EditableEvent(
            id = id,
            dateTime = published,
            coordinates = coordinates,
            type = type,
            attachment = attachment,
            link = link?.url,
            speakers = speakerIds,
        )
    }

    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            _editableEvent.postValue(
                eventRepository.getById(eventId)?.toEditable()
                    ?: throw NullPointerException("event $eventId not found")
            )
        }
    }
}