package ru.rainman.domain.dto

import ru.rainman.domain.model.MediaSource

data class NewAttachmentDto(
    val type: Type,
    val media: MediaSource
) {
    enum class Type {
        IMAGE, AUDIO, VIDEO
    }
}
