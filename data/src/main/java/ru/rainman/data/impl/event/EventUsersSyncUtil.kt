package ru.rainman.data.impl.event

import ru.rainman.data.impl.PublicationUsersSyncUtil
import ru.rainman.data.local.dao.EventDao
import ru.rainman.data.local.entity.crossref.EventsLikeOwnersCrossRef
import ru.rainman.data.local.entity.crossref.EventsParticipantsCrossRef
import ru.rainman.data.remote.response.EventResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventUsersSyncUtil @Inject constructor(
    private val eventDao: EventDao
) : PublicationUsersSyncUtil() {

    suspend fun syncEventLikeOwners(event: EventResponse) {
        val newList = event.likeOwnerIds.map {
            EventsLikeOwnersCrossRef(
                event.id, it
            )
        }
        val oldList = eventDao.getLikeOwners(event.id)
        eventDao.updateLikeOwners(calcDiff(newList, oldList))
    }

    suspend fun syncEventParticipants(event: EventResponse) {
        val newList = event.participantsIds.map {
            EventsParticipantsCrossRef(
                event.id, it
            )
        }
        val oldList = eventDao.getParticipants(event.id)
        eventDao.updateParticipants(calcDiff(newList, oldList))
    }

}

