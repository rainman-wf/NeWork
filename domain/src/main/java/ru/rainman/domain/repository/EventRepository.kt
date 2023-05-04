package ru.rainman.domain.repository

import ru.rainman.domain.dto.NewObjectDto
import ru.rainman.domain.model.BaseModel

interface EventRepository<M : BaseModel, NO : NewObjectDto> : PublicationsRepository<M, NO> {

    suspend fun participate(id: Long) : M
}