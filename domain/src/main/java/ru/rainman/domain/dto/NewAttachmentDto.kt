package ru.rainman.domain.dto

import ru.rainman.domain.model.UploadMedia

data class NewAttachmentDto(
    val type: Type,
    val media: UploadMedia
) {
    enum class Type {
        IMAGE, AUDIO, VIDEO
    }
}
