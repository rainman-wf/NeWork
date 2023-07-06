package ru.rainman.data.impl.job

import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rainman.data.apiRequest
import ru.rainman.data.dbQuery
import ru.rainman.data.formatLink
import ru.rainman.data.impl.toEntity
import ru.rainman.data.impl.toModel
import ru.rainman.data.impl.toRequestBody
import ru.rainman.data.impl.user.LinkPreviewUtil
import ru.rainman.data.isUrl
import ru.rainman.data.local.dao.JobDao
import ru.rainman.data.local.entity.JobEntity
import ru.rainman.data.remote.api.JobApi
import ru.rainman.data.remote.response.JobResponse
import ru.rainman.domain.dto.NewJobDto
import ru.rainman.domain.model.Job
import ru.rainman.domain.repository.JobRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val jobApi: JobApi,
    private val linkPreviewUtil: LinkPreviewUtil
): JobRepository {

    override fun jobs(userId: Long): Flow<List<Job>> = jobDao.getUserJobs(userId).map { it.map { job -> job.toModel() } }

    override suspend fun delete(jobId: Long) {
        apiRequest { jobApi.delete(jobId) }
        dbQuery { jobDao.delete(listOf(jobId)) }
    }

    override val data: Flow<PagingData<Job>> = emptyFlow()

    override suspend fun create(newObjectDto: NewJobDto) {
        val response = apiRequest { jobApi.create(newObjectDto.toRequestBody()) }
        val entity = response.toEntity(newObjectDto.ownerId)
        withContext(Dispatchers.IO){
            dbQuery { jobDao.insert(response.toEntity(newObjectDto.ownerId)) }
        }
        repositoryScope.launch {
            syncLink(response, entity)
        }
    }

    override suspend fun getById(id: Long): Job? {
        return jobDao.getById(id)?.toModel()
    }

    override suspend fun getByIds(ids: List<Long>): List<Job> {
        return emptyList()
    }

    private suspend fun syncLink(jobResponse: JobResponse, entity: JobEntity) {

        val linkKey = entity.link

        when {
            jobResponse.link == null -> linkKey?.let { jobDao.deleteLink(it) }
            !jobResponse.link.formatLink().isUrl() -> linkKey?.let { jobDao.deleteLink(it) }
            else ->
                if (linkKey != null) {
                    if (jobDao.getLinkPreviewUrl(linkKey) != jobResponse.link) {
                        jobDao.deleteLink(linkKey)
                        linkPreviewUtil.getLinkPreviewEntity(jobResponse)?.let {
                            jobDao.insertLinkPreview(
                                jobResponse.id,
                                it
                            )
                        }
                    }
                } else {
                    linkPreviewUtil.getLinkPreviewEntity(jobResponse)?.let {
                        jobDao.insertLinkPreview(
                            jobResponse.id,
                            it
                        )
                    }
                }
        }
    }
}