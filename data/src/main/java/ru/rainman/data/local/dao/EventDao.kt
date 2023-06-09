package ru.rainman.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.rainman.data.local.entity.AttachmentEntity
import ru.rainman.data.local.entity.EventAttachmentEntity
import ru.rainman.data.local.entity.EventEntity
import ru.rainman.data.local.entity.EventLinkPreviewEntity
import ru.rainman.data.local.entity.EventWithUsers
import ru.rainman.data.local.entity.crossref.*
import ru.rainman.data.local.utils.PublicationUsersDiff

@Dao
interface EventDao : BaseDao<EventEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: EventAttachmentEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikeOwners(ids: List<EventsLikeOwnersCrossRef>)

    @Query("SELECT liked_by_me FROM events WHERE event_id = :eventId")
    suspend fun likedByMe(eventId: Long) : Boolean

    @Query("SELECT participated_by_me FROM events WHERE event_id = :eventId")
    suspend fun participatedByMe(eventId: Long) : Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeakers(ids: List<EventsSpeakersCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(ids: List<EventsParticipantsCrossRef>)

    @Transaction
    @Query("SELECT * FROM events ORDER by event_id DESC")
    fun getPaged(): PagingSource<Int, EventWithUsers>

    @Transaction
    @Query("SELECT * FROM events WHERE event_id = :eventId")
    suspend fun getById(eventId: Long) : EventWithUsers?

    @Query("SELECT * FROM events_like_owners_cross_refs WHERE event_id = :eventId")
    suspend fun getLikeOwners(eventId: Long) : List<EventsLikeOwnersCrossRef>

    @Query("SELECT * FROM events_participants_cross_refs WHERE event_id = :eventId")
    suspend fun getParticipants(eventId: Long) : List<EventsParticipantsCrossRef>

    @Delete
    fun deleteLikeOwners(list: List<EventsLikeOwnersCrossRef>)

    @Delete
    fun deleteParticipants(list: List<EventsParticipantsCrossRef>)

    @Transaction
    suspend fun updateLikeOwners(
        publicationUsersDiff: PublicationUsersDiff<EventsLikeOwnersCrossRef>
    ) {
        insertLikeOwners(publicationUsersDiff.toInsert)
        deleteLikeOwners(publicationUsersDiff.toDelete)
    }

    @Transaction
    suspend fun updateParticipants(
        publicationUsersDiff: PublicationUsersDiff<EventsParticipantsCrossRef>
    ) {
        insertParticipants(publicationUsersDiff.toInsert)
        deleteParticipants(publicationUsersDiff.toDelete)
    }


    @Insert
    fun insertLinkPreview(linkPreview: List<EventLinkPreviewEntity>)

    @Transaction
    suspend fun batchInsert(
        entity: List<EventEntity>,
        likeOwners: List<EventsLikeOwnersCrossRef>,
        speakers: List<EventsSpeakersCrossRef>,
        participants: List<EventsParticipantsCrossRef>
    ) {
        insert(entity)
        insertLikeOwners(likeOwners)
        insertSpeakers(speakers)
        insertParticipants(participants)
    }

    @Query("DELETE FROM events WHERE event_id = :id")
    suspend fun delete(id: Long)
}


