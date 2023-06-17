package ru.rainman.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import ru.rainman.data.local.entity.crossref.EventsLikeOwnersCrossRef
import ru.rainman.data.local.entity.crossref.EventsParticipantsCrossRef
import ru.rainman.data.local.entity.crossref.EventsSpeakersCrossRef

data class EventWithUsers (

    @Embedded
    val eventEntity: EventEntity,

    @Relation(entity = UserEntity::class, parentColumn = "author_id", entityColumn = "user_id")
    val author: UserWithJob,

    @Relation(parentColumn = "attachment_key", entityColumn = "key")
    val attachment: AttachmentEntity?,

    @Relation(parentColumn = "link_key", entityColumn = "key")
    val linkPreview: LinkPreviewEntity?,

    @Relation(
        entity = UserEntity::class,
        parentColumn = "event_id",
        entityColumn = "user_id",
        associateBy = Junction(EventsLikeOwnersCrossRef::class)
    )
    val likeOwners: List<UserWithJob>,

    @Relation(
        entity = UserEntity::class,
        parentColumn = "event_id",
        entityColumn = "user_id",
        associateBy = Junction(EventsSpeakersCrossRef::class)
    )
    val speakers: List<UserWithJob>,

    @Relation(
        entity = UserEntity::class,
        parentColumn = "event_id",
        entityColumn = "user_id",
        associateBy = Junction(EventsParticipantsCrossRef::class)
    )
    val participants: List<UserWithJob>,
)