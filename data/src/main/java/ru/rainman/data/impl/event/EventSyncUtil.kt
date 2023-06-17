package ru.rainman.data.impl.event

import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rainman.common.log
import ru.rainman.data.dbQuery
import ru.rainman.data.impl.AttachmentsUtil
import ru.rainman.data.impl.fetchEventLikeOwners
import ru.rainman.data.impl.fetchParticipants
import ru.rainman.data.impl.fetchSpeakers
import ru.rainman.data.impl.toEntity
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.EventDao
import ru.rainman.data.local.entity.EventEntity
import ru.rainman.data.local.entity.crossref.EventsLikeOwnersCrossRef
import ru.rainman.data.local.entity.crossref.EventsParticipantsCrossRef
import ru.rainman.data.local.entity.crossref.EventsSpeakersCrossRef
import ru.rainman.data.remote.response.EventResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventSyncUtil @Inject constructor(
    private val eventDao: EventDao,
    private val db: AppDb,
    private val attachmentsUtil: AttachmentsUtil
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun sync(response: List<EventResponse>, range: LongRange?) {

        val existEvents = range?.let { eventDao.getEventIdsInRange(it.toList()) }

        val responseIds = response.map { it.id }
        val likeOwners = response.fetchEventLikeOwners()
        val speakers = response.fetchSpeakers()
        val participants = response.fetchParticipants()

        if (existEvents.isNullOrEmpty()) {
            eventDao.upsertEvents(
                response.map { it.toEntity() },
                likeOwners,
                speakers,
                participants
            )
        } else {

            val deletableEvents = existEvents.minus(responseIds.toSet())
            val existedLikes = eventDao.getEventsLikeOwners(responseIds)
            val existedParticipants = eventDao.getEventsParticipants(responseIds)
            val existedSpeakers = eventDao.getEventsSpeakers(responseIds)

            val deletableLikes = existedLikes.minus(likeOwners.toSet())
            val deletableParticipants = existedParticipants.minus(participants.toSet())
            val deletableSpeakers = existedSpeakers.minus(speakers.toSet())

            dbQuery {
                db.withTransaction {
                    eventDao.delete(deletableEvents)
                    eventDao.deleteLikeOwners(deletableLikes)
                    eventDao.deleteParticipants(deletableParticipants)
                    eventDao.deleteSpeakers(deletableSpeakers)

                    eventDao.upsertEvents(
                        response.map { it.toEntity() },
                        likeOwners,
                        speakers,
                        participants
                    )
                }
            }

            response.filter { it.attachment != null && existEvents.contains(it.id) }.forEach {
                scope.launch {
                    syncAttachment(it, dbQuery { eventDao.getPureEntityById(it.id)!! })
                }
            }

        }
    }

    suspend fun sync(response: EventResponse) {

        val existEvent = eventDao.getById(response.id)

        val likeOwners = response.likeOwnerIds.map { EventsLikeOwnersCrossRef(response.id, it) }
        val speakers = response.speakerIds.map { EventsSpeakersCrossRef(response.id, it) }
        val participants =
            response.participantsIds.map { EventsParticipantsCrossRef(response.id, it) }

        if (existEvent == null) {
            eventDao.upsertEvents(listOf(response.toEntity()), likeOwners, speakers, participants)
        } else {

            val existedLikes = eventDao.getEventLikeOwners(response.id)
            val existedParticipants = eventDao.getEventParticipants(response.id)
            val existedSpeakers = eventDao.getEventSpeakers(response.id)

            val deletableLikes = existedLikes.minus(likeOwners.toSet())
            val deletableParticipants = existedParticipants.minus(participants.toSet())
            val deletableSpeakers = existedSpeakers.minus(speakers.toSet())

            dbQuery {
                db.withTransaction {
                    eventDao.deleteLikeOwners(deletableLikes)
                    eventDao.deleteParticipants(deletableParticipants)
                    eventDao.deleteSpeakers(deletableSpeakers)

                    eventDao.upsertEvents(
                        listOf(response.toEntity()),
                        likeOwners,
                        speakers,
                        participants
                    )
                }
            }


            scope.launch {
                syncAttachment(response, dbQuery { eventDao.getPureEntityById(response.id)!! })
            }
        }
    }

    private suspend fun syncAttachment(eventResponse: EventResponse, entity: EventEntity) {

        val isAttachmentCorrect = eventResponse.attachment?.url?.log()?.startsWith("http")
        val attachmentKey = entity.attachmentKey

        when {
            eventResponse.attachment == null ->
                attachmentKey?.let { eventDao.deleteAttachment(it) }

            isAttachmentCorrect == false ->
                attachmentKey?.let { eventDao.deleteAttachment(it) }

            isAttachmentCorrect == true ->
                attachmentKey?.let {
                if (eventDao.getAttachmentUrl(it) != eventResponse.attachment.url)

                    eventDao.deleteAttachment(it)

                    attachmentsUtil.getAttachmentEntityFrom(eventResponse)?.let { entity ->
                        eventDao.insertAttachment(
                            eventResponse.id,
                            entity
                        )
                    }

                }

        }

    }
}