package ru.rainman.data.impl.user

import ru.rainman.data.impl.compareWith
import ru.rainman.data.impl.toEntity
import ru.rainman.data.local.dao.JobDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.remote.api.JobApi
import ru.rainman.data.remote.apiRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersJobsSyncUtil @Inject constructor(
    private val userDao: UserDao,
    private val jobApi: JobApi,
    private val jobDao: JobDao
) {

    suspend fun sync(userIds: Set<Long>) {

        val remoteJobs = userIds
            .map { id ->
                apiRequest { jobApi.getUserJobs(id) }
                    .map { it.toEntity(id) }
            }

        remoteJobs.forEach { jobs ->
            jobs.findLast { it.finish == null }?.let {
                userDao.updateJobId(it.employeeId, it.id)
            }
        }

        val flattenJobs = remoteJobs.flatten().toSet()

        val existedJobs = jobDao.getUsersJobs(flattenJobs.map { it.employeeId })

        val newJobs = flattenJobs.minus(existedJobs.toSet()).toList()

        val updatedJobs = existedJobs.filter { existed ->
            existed.compareWith(flattenJobs.singleOrNull { existed.id == it.id })
        }

        jobDao.insert(newJobs)
        jobDao.update(updatedJobs)
    }
}