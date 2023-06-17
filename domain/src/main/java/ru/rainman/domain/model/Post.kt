package ru.rainman.domain.model

import java.time.LocalDateTime

data class Post(
    override val id: Long,
    override val author: User,
    override val content: String,
    override val published: LocalDateTime,
    override val coordinates: Coordinates?,
    override val link: LinkPreview?,
    override val likeOwnerIds: List<User>,
    val mentioned: List<User>,
    val mentionedMe: Boolean,
    override val likedByMe: Boolean,
    override val attachment: Attachment?,
    override val ownedByMe: Boolean,
) : Publication
