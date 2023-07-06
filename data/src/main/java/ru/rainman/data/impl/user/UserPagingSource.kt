package ru.rainman.data.impl.user

import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.rainman.data.apiRequest
import ru.rainman.data.impl.CustomPagingSource
import ru.rainman.data.impl.toEntity
import ru.rainman.data.impl.toModel
import ru.rainman.data.local.dao.JobDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.local.entity.UserWithJob
import ru.rainman.data.remote.api.JobApi
import ru.rainman.domain.model.User


class UserPagingSource(
    private val userDao: UserDao,
    private val jobDao: JobDao,
    private val jobApi: JobApi
) : CustomPagingSource<Int, User, UserWithJob>() {

    override fun getRefreshKey(state: PagingState<Int, User>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val nextPageNumber = params.key ?: 0
        val users = userDao.getSized(nextPageNumber, params.loadSize)

        CoroutineScope(Dispatchers.IO).launch {
            users.forEach { user ->
                val jobs = apiRequest { jobApi.getUserJobs(user.userEntity.userId) }.map { it.toEntity(user.userEntity.userId) }
                if (jobs.isNotEmpty()) jobDao.insert(jobs)
            }
        }

        return result(users, nextPageNumber, params.loadSize) {
            it.toModel()
        }
    }
}