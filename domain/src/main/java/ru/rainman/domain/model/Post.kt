package ru.rainman.domain.model

import java.time.LocalDateTime

data class Post(
    override val id: Long,
    val author: User,
    val content: String,
    val published: LocalDateTime,
    val coordinates: Coordinates?,
    val link: LinkPreview?,
    val likeOwnerIds: List<User>,
    val mentionIds: List<User>,
    val mentionedMe: Boolean,
    val likedByMe: Boolean,
    val attachment: Attachment?,
    val ownedByMe: Boolean,
) : BaseModel
