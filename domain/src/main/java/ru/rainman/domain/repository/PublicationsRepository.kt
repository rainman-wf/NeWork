package ru.rainman.domain.repository

import ru.rainman.domain.dto.NewObjectDto
import ru.rainman.domain.model.BaseModel

interface PublicationsRepository<M : BaseModel, NO : NewObjectDto> : BaseRepository<M, NO> {

    suspend fun delete(id: Long)
    suspend fun like(id: Long)
    suspend fun unlike(id: Long)

}