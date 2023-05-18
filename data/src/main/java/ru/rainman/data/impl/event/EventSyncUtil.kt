package ru.rainman.data.impl.event

import ru.rainman.data.impl.compareWith
import ru.rainman.data.impl.toEntity
import ru.rainman.data.local.dao.EventDao
import ru.rainman.data.local.entity.EventWithUsers
import ru.rainman.data.remote.response.EventResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventSyncUtil @Inject constructor(
    private val eventDao: EventDao,
    private val eventUsersSyncUtil: EventUsersSyncUtil
) {

    suspend fun sync(eventResponse: EventResponse): EventWithUsers {

        val newEvent = eventResponse.toEntity()
        val oldEvent = eventDao.getById(eventResponse.id)

        if (!oldEvent.eventEntity.compareWith(newEvent)) eventDao.update(newEvent)

        eventUsersSyncUtil.syncEventLikeOwners(eventResponse)
        eventUsersSyncUtil.syncEventParticipants(eventResponse)

        return eventDao.getById(eventResponse.id)
    }
}