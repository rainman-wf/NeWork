package ru.rainman.ui.helperutils

import ru.rainman.domain.dto.NewObjectDto
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates
import ru.rainman.domain.model.EventType
import ru.rainman.domain.model.User
import java.time.LocalDateTime

data class EditableEvent(
    val id: Long = 0,
    val dateTime: LocalDateTime,
    val coordinates: Coordinates? = null,
    val type: EventType = EventType.ONLINE,
    val attachment: Attachment? = null,
    val link: String? = null,
    val speakers: List<User> = mutableListOf()
) : NewObjectDto