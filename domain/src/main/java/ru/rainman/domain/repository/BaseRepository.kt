package ru.rainman.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import ru.rainman.domain.dto.NewObjectDto
import ru.rainman.domain.model.BaseModel

interface BaseRepository<M : BaseModel, NO : NewObjectDto> {
    val repositoryScope: CoroutineScope get() = CoroutineScope(Dispatchers.IO)

    val data: Flow<PagingData<M>>

    suspend fun create(newObjectDto: NO) : M?
    suspend fun getById(id: Long): M?
    suspend fun getByIds(ids: List<Long>) : List<M>
}

