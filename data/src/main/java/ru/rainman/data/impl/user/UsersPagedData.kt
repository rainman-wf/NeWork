package ru.rainman.data.impl.user

import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import ru.rainman.data.local.dao.JobDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.remote.api.JobApi

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersPagedData @Inject constructor(
    userDao: UserDao,
    jobDao: JobDao,
    jobApi: JobApi
) {
    val data = Pager(PagingConfig(10)) {
        UserPagingSource(userDao, jobDao, jobApi)
    }.flow.flowOn(Dispatchers.IO)
}