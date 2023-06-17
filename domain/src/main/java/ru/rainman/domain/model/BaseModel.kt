package ru.rainman.domain.model

import java.time.LocalDateTime

interface BaseModel {
    val id: Long
}

interface Publication : BaseModel {
    val author: User
    val content: String
    val published: LocalDateTime
    val coordinates: Coordinates?
    val likeOwnerIds: List<User>
    val likedByMe: Boolean
    val attachment: Attachment?
    val link: LinkPreview?
    val ownedByMe: Boolean
}
