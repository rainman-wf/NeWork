package ru.rainman.domain.dto

import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates

data class NewPostDto(
    val content: String,
    val coordinates: Coordinates? = null,
    val link: String? = null,
    val attachment: Attachment? = null,
    val mentionIds: List<Long> = listOf()
): NewObjectDto

