package ru.rainman.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.rainman.data.local.entity.AttachmentEntity
import ru.rainman.data.local.entity.EventEntity
import ru.rainman.data.local.entity.LinkPreviewEntity
import ru.rainman.data.local.entity.EventWithUsers
import ru.rainman.data.local.entity.PostEntity
import ru.rainman.data.local.entity.crossref.*
import ru.rainman.data.local.utils.PublicationUsersDiff

@Dao
interface EventDao : BaseDao<EventEntity> {

    @Upsert
    suspend fun upsertAttachment(attachment: AttachmentEntity) : Long

    @Query("UPDATE events SET attachment_key = :attachmentKey WHERE event_id = :eventId")
    suspend fun setAttachment(attachmentKey: Long, eventId: Long)

    @Transaction
    suspend fun insertAttachment(eventId: Long, attachment: AttachmentEntity) {
        val generatedKey = upsertAttachment(attachment)
        setAttachment(generatedKey, eventId)
    }

    @Upsert
    suspend fun upsertLinkPreview(link: LinkPreviewEntity) : Long

    @Query("UPDATE events SET link_key = :linkKey WHERE event_id = :eventId")
    suspend fun setLink(linkKey: Long, eventId: Long)

    @Transaction
    suspend fun insertLinkPreview(eventId: Long, link: LinkPreviewEntity) {
        val generatedKey = upsertLinkPreview(link)
        setLink(generatedKey, eventId)
    }

    @Query("SELECT event_id FROM events WHERE event_id IN(:eventIds)")
    suspend fun getEventIdsInRange(eventIds: List<Long>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLikeOwners(ids: List<EventsLikeOwnersCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSpeakers(ids: List<EventsSpeakersCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParticipants(ids: List<EventsParticipantsCrossRef>)

    @Transaction
    @Query("SELECT * FROM events ORDER by event_id DESC")
    fun getPaged(): PagingSource<Int, EventWithUsers>

    @Transaction
    @Query("SELECT * FROM events WHERE event_id = :eventId")
    suspend fun getById(eventId: Long) : EventWithUsers?

    @Delete
    fun deleteLikeOwners(list: List<EventsLikeOwnersCrossRef>)

    @Delete
    fun deleteParticipants(list: List<EventsParticipantsCrossRef>)

    @Delete
    fun deleteSpeakers(list: List<EventsSpeakersCrossRef>)

    @Transaction
    @Upsert
    suspend fun upsertEvents(
        entity: List<EventEntity>,
        likeOwners: List<EventsLikeOwnersCrossRef>,
        speakers: List<EventsSpeakersCrossRef>,
        participants: List<EventsParticipantsCrossRef>
    )

    @Query("DELETE FROM events WHERE event_id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM events WHERE event_id IN(:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("SELECT * FROM events_like_owners_cross_refs WHERE event_id IN(:eventIds)")
    suspend fun getEventsLikeOwners(eventIds: List<Long>) : List<EventsLikeOwnersCrossRef>

    @Query("SELECT * FROM events_like_owners_cross_refs WHERE event_id = :eventId")
    suspend fun getEventLikeOwners(eventId: Long) : List<EventsLikeOwnersCrossRef>

    @Query("SELECT * FROM events_participants_cross_refs WHERE event_id IN(:eventIds)")
    suspend fun getEventsParticipants(eventIds: List<Long>) : List<EventsParticipantsCrossRef>

    @Query("SELECT * FROM events_participants_cross_refs WHERE event_id = :eventId")
    suspend fun getEventParticipants(eventId: Long) : List<EventsParticipantsCrossRef>

    @Query("SELECT * FROM events_speakers_cross_refs WHERE event_id IN(:eventIds)")
    suspend fun getEventsSpeakers(eventIds: List<Long>) : List<EventsSpeakersCrossRef>

    @Query("SELECT * FROM events_speakers_cross_refs WHERE event_id = :eventId")
    suspend fun getEventSpeakers(eventId: Long) : List<EventsSpeakersCrossRef>

    @Query("SELECT * FROM events WHERE event_id = :eventId")
    suspend fun getPureEntityById(eventId: Long): EventEntity?


    @Query("DELETE FROM attachments WHERE `key` = :key")
    suspend fun deleteAttachment(key: Long)

    @Query("SELECT url FROM attachments WHERE `key` = :key")
    suspend fun getAttachmentUrl(key: Long) : String?
}


