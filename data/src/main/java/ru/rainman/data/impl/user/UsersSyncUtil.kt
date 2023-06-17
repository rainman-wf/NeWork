package ru.rainman.data.impl.user

import androidx.room.withTransaction
import ru.rainman.data.apiRequest
import ru.rainman.data.dbQuery
import ru.rainman.data.impl.toEntity
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.JobDao
import ru.rainman.data.local.entity.UserEntity
import ru.rainman.data.remote.api.JobApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersSyncUtil @Inject constructor(
    private val jobApi: JobApi,
    private val jobDao: JobDao,
    private val db: AppDb
) {

    suspend fun sync(users: Set<UserEntity>) {

        val remoteJobs = users
            .map { it.userId }
            .map { id ->
                apiRequest { jobApi.getUserJobs(id) }
                    .map { it.toEntity(id) }
            }.flatten().toSet()

        val existedJobs = jobDao.getUsersJobs(remoteJobs.map { it.employeeId })

        val toDelete = existedJobs.minus(remoteJobs.toSet()).toList()

        dbQuery {
            db.withTransaction {
                jobDao.upsert(remoteJobs.toList())
                jobDao.delete(toDelete)
            }
        }

    }

}