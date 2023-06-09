package ru.rainman.domain.dto

import ru.rainman.domain.model.Coordinates

data class NewPostDto(
    val id: Long = 0,
    val content: String,
    val coordinates: Coordinates? = null,
    val link: String? = null,
    val attachment: NewAttachmentDto? = null,
    val mentionIds: List<Long> = listOf()
): NewObjectDto

