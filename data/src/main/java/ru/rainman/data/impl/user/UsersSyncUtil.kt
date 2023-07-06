package ru.rainman.data.impl.user

import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rainman.data.apiRequest
import ru.rainman.data.dbQuery
import ru.rainman.data.formatLink
import ru.rainman.data.impl.toEntity
import ru.rainman.data.isUrl
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.JobDao
import ru.rainman.data.local.entity.JobEntity
import ru.rainman.data.local.entity.UserEntity
import ru.rainman.data.remote.api.JobApi
import ru.rainman.data.remote.response.JobResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersSyncUtil @Inject constructor(
    private val jobApi: JobApi,
    private val jobDao: JobDao,
    private val db: AppDb,
    private val linkPreviewUtil: LinkPreviewUtil
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun sync(users: Set<UserEntity>) {

        val remoteJobs = users
            .map { user -> user.userId to apiRequest { jobApi.getUserJobs(user.userId) } }
            .map { pair ->
                pair.second.map {
                    pair.first to it
                }
            }.flatten()


        val existedJobs = jobDao.getJobs(remoteJobs.map { it.first }.toSet().toList())


        if (existedJobs.isEmpty()) {
            jobDao.upsert(remoteJobs.map { it.second.toEntity(it.first) })

            remoteJobs
                .filter { it.second.link != null && it.second.link!!.startsWith("http") }
                .forEach {
                    scope.launch {
                        linkPreviewUtil.getLinkPreviewEntity(it.second)?.let { linkPreviewEntity ->
                            jobDao.insertLinkPreview(
                                it.second.id,
                                linkPreviewEntity
                            )
                        }
                    }
                }
        } else {

            val existedIds = existedJobs.map { it.id }

            val deletableJobs = existedIds.minus(remoteJobs.map { it.second.id }.toSet()).toList()

            val updatableJobs = remoteJobs.map { it.second.toEntity(it.first) }.minus(existedJobs.toSet()).map {
                it.copy(
                    link = existedJobs.singleOrNull { entity -> entity.id == it.id }?.link
                )
            }

            dbQuery {
                db.withTransaction {
                    jobDao.delete(deletableJobs)
                    jobDao.upsert(updatableJobs)
                }
            }

            scope.launch {
                remoteJobs
                    .filter {
                        it.second.link != null && existedIds.contains(it.second.id)
                    }
                    .forEach { pair ->
                        existedJobs.singleOrNull { it.id == pair.second.id }?.let { syncLink(pair.second, it) }
                    }
            }
        }

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