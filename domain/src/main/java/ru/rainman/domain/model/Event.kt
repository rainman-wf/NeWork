package ru.rainman.domain.model

import java.time.LocalDateTime

data class Event(
    override val id: Long,
    val author: User,
    val content: String,
    val datetime: LocalDateTime,
    val published: LocalDateTime,
    val coordinates: Coordinates?,
    val type: EventType,
    val likeOwnerIds: List<User>,
    val likedByMe: Boolean,
    val speakerIds: List<User>,
    val participantsIds: List<User>,
    val participatedByMe: Boolean,
    val attachment: Attachment?,
    val link: LinkPreview?,
    val ownedByMe: Boolean
) : BaseModel