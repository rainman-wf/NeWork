package ru.rainman.ui.helperutils

import ru.rainman.domain.dto.NewObjectDto
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates
import ru.rainman.domain.model.EventType
import ru.rainman.domain.model.User
import java.time.LocalDate
import java.time.LocalDateTime

data class EditableJob(
    val id: Long = 0,
    val ownerId: Long,
    val start: LocalDate = LocalDate.now(),
    val finish: LocalDate? = null,
) : NewObjectDto