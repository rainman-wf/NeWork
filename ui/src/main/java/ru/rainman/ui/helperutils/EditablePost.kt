package ru.rainman.ui.helperutils

import ru.rainman.domain.dto.NewObjectDto
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates
import ru.rainman.domain.model.User

data class EditablePost(
    val id: Long = 0,
    val coordinates: Coordinates? = null,
    val link: String? = null,
    val attachment: Attachment? = null,
    val mentioned: List<User> = listOf()
): NewObjectDto

