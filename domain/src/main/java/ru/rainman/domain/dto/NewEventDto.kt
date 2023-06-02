package ru.rainman.domain.dto

import ru.rainman.domain.model.Coordinates
import ru.rainman.domain.model.EventType
import java.time.LocalDateTime

data class NewEventDto(
    val content: String,
    val dateTime: LocalDateTime,
    val coordinates: Coordinates? = null,
    val type: EventType? = null,
    val attachment: NewAttachmentDto? = null,
    val link: String? = null,
    val speakerIds: List<Long>
) : NewObjectDto