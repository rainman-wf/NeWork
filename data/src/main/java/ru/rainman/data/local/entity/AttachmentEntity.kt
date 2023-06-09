package ru.rainman.data.local.entity

open class AttachmentEntity(
    open val publicationId: Long,
    open val url: String,
    open val type: AttachmentType,
    open val duration: Int?,
    open val ratio: Float?,
    open val artist: String?,
    open val title: String?
)