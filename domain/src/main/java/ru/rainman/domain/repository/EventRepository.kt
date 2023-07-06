package ru.rainman.domain.repository

import ru.rainman.domain.dto.NewEventDto
import ru.rainman.domain.model.Event

interface EventRepository : PublicationsRepository<Event, NewEventDto> {
    suspend fun participate(id: Long)
}