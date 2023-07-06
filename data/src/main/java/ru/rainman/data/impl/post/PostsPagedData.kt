package ru.rainman.data.impl.post

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.rainman.common.PAGINATION_LOAD_SIZE
import ru.rainman.common.log
import ru.rainman.data.impl.toModel
import ru.rainman.data.local.dao.PostDao
import ru.rainman.domain.model.Post
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostsPagedData @Inject constructor(
    private val postDao: PostDao,
    postRemoteMediator: PostsRemoteMediator,
    private val wallRemoteMediator: WallRemoteMediator,
) {

    @OptIn(ExperimentalPagingApi::class)
    fun wall(ownerId: Long) = Pager(
        config = PagingConfig(
            pageSize = PAGINATION_LOAD_SIZE,
            prefetchDistance = PAGINATION_LOAD_SIZE,
            enablePlaceholders = true,
            maxSize = 3 * PAGINATION_LOAD_SIZE
        ),
        pagingSourceFactory = { postDao.getPagedWall(ownerId) },
        remoteMediator = wallRemoteMediator
    ).flow.map {
        it.map { e -> e.toModel() }
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalPagingApi::class)
    val data = Pager(
        config = PagingConfig(
            pageSize = PAGINATION_LOAD_SIZE,
            prefetchDistance = PAGINATION_LOAD_SIZE,
            enablePlaceholders = true,
            maxSize = 3 * PAGINATION_LOAD_SIZE
        ),
        pagingSourceFactory = { postDao.getPaged() },
        remoteMediator = postRemoteMediator
    ).flow.map {
        it.map { e -> e.toModel() }
    }.flowOn(Dispatchers.IO)

}
