package ru.rainman.domain.model

import java.time.LocalDateTime

data class Event(
    override val id: Long,
    override val author: User,
    override val content: String,
    val datetime: LocalDateTime,
    override val published: LocalDateTime,
    override val coordinates: Coordinates?,
    val type: EventType,
    override val likeOwnerIds: List<User>,
    override val likedByMe: Boolean,
    val speakerIds: List<User>,
    val participantsIds: List<User>,
    val participatedByMe: Boolean,
    override val attachment: Attachment?,
    override val link: LinkPreview?,
    override val ownedByMe: Boolean
) : Publication