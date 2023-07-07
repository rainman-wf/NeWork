package ru.rainman.data.impl.event

import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rainman.data.dbQuery
import ru.rainman.data.formatLink
import ru.rainman.data.impl.AttachmentsUtil
import ru.rainman.data.impl.fetchEventLikeOwners
import ru.rainman.data.impl.fetchParticipants
import ru.rainman.data.impl.fetchSpeakers
import ru.rainman.data.impl.toEntity
import ru.rainman.data.impl.user.LinkPreviewUtil
import ru.rainman.data.isUrl
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
    private val attachmentsUtil: AttachmentsUtil,
    private val linkPreviewUtil: LinkPreviewUtil
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun sync(response: List<EventResponse>, range: LongRange?) {

        val existEvents = range?.let { eventDao.getEventsByIds(it.toList()) }

        val responseIds = response.map { it.id }
        val likeOwners = response.fetchEventLikeOwners()
        val speakers = response.fetchSpeakers()
        val participants = response.fetchParticipants()

        val newEvents = response.map { it.toEntity() }

        if (existEvents.isNullOrEmpty()) {
            eventDao.upsertEvents(
                response.map { it.toEntity() },
                likeOwners,
                speakers,
                participants
            )

            response
                .filter { it.attachment != null && it.attachment.url.startsWith("http") }
                .forEach {
                    scope.launch {
                        attachmentsUtil.getAttachmentEntityFrom(it)?.let { entity ->
                            eventDao.insertAttachment(
                                it.id,
                                entity
                            )
                        }
                    }
                }

            response.filter { it.attachment != null }.forEach {
                scope.launch {
                    syncAttachment(it, dbQuery { eventDao.getPureEntityById(it.id)!! })
                }
            }

            scope.launch {
                response.filter { it.link != null }.forEach {
                    syncLink(it, dbQuery { eventDao.getPureEntityById(it.id)!! })
                }
            }
        } else {

            val existedIds = existEvents.map { it.id }

            val deletableEvents = existedIds.minus(responseIds.toSet())
            val existedLikes = eventDao.getEventsLikeOwners(responseIds)
            val existedParticipants = eventDao.getEventsParticipants(responseIds)
            val existedSpeakers = eventDao.getEventsSpeakers(responseIds)

            val updatableEvents = newEvents.minus(existEvents).map {
                it.copy(attachmentKey = existEvents.singleOrNull { eventEntity ->
                    eventEntity.id == it.id
                }?.attachmentKey)
            }

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
                        updatableEvents,
                        likeOwners,
                        speakers,
                        participants
                    )
                }
            }


            scope.launch {
                response.filter { existedIds.contains(it.id) }.forEach {
                    syncAttachment(it, it.toEntity())
                }
            }

            scope.launch {
                response.filter { existedIds.contains(it.id) }.forEach {
                    syncLink(it, it.toEntity())
                }
            }

        }
    }

    private suspend fun syncLink(eventResponse: EventResponse, entity: EventEntity) {

        val linkKey = entity.linkKey

        when {
            eventResponse.link == null -> linkKey?.let { eventDao.deleteLink(it) }
            !eventResponse.link.formatLink().isUrl() -> linkKey?.let { eventDao.deleteLink(it) }
            else ->
                if (linkKey != null) {
                    if (eventDao.getLinkPreviewUrl(linkKey) != eventResponse.link) {
                        eventDao.deleteLink(linkKey)
                        linkPreviewUtil.getLinkPreviewEntity(eventResponse)?.let {
                            eventDao.insertLinkPreview(
                                eventResponse.id,
                                it
                            )
                        }
                    }
                } else {
                    linkPreviewUtil.getLinkPreviewEntity(eventResponse)?.let {
                        eventDao.insertLinkPreview(
                            eventResponse.id,
                            it
                        )
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

            scope.launch {
                attachmentsUtil.getAttachmentEntityFrom(response)?.let { entity ->
                    eventDao.insertAttachment(
                        response.id,
                        entity
                    )
                }
            }

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
                        listOf(
                            response.toEntity().copy(attachmentKey = existEvent.attachment?.key)
                        ),
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

        val isAttachmentCorrect = eventResponse.attachment?.url?.startsWith("http") == true
        val attachmentKey = entity.attachmentKey

        when {
            eventResponse.attachment == null ->
                attachmentKey?.let { eventDao.deleteAttachment(it) }

            !isAttachmentCorrect ->
                attachmentKey?.let { eventDao.deleteAttachment(it) }

            else ->
                attachmentKey?.let { key ->
                    if (eventDao.getAttachmentUrl(key) != eventResponse.attachment.url) {

                        eventDao.deleteAttachment(key)

                        attachmentsUtil.getAttachmentEntityFrom(eventResponse)?.let {
                            eventDao.insertAttachment(
                                eventResponse.id,
                                it
                            )
                        }
                    } else {
                        attachmentsUtil.getAttachmentEntityFrom(eventResponse)?.let {
                            eventDao.insertAttachment(
                                eventResponse.id,
                                it
                            )
                        }
                    }
                }
        }
    }
}