package ru.rainman.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.rainman.domain.dto.NewJobDto
import ru.rainman.domain.model.Job

interface JobRepository : BaseRepository<Job, NewJobDto> {

    fun jobs(userId: Long): Flow<List<Job>>
    suspend fun delete(jobId: Long)
}