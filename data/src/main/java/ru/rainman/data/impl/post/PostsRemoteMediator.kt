package ru.rainman.data.impl.post

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.rainman.common.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rainman.common.POSTS_REMOTE_KEYS
import ru.rainman.data.apiRequest
import ru.rainman.data.impl.user.UsersSyncUtil
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.RemoteKeyDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.local.entity.PostWithUsers
import ru.rainman.data.local.entity.RemoteKeysEntity
import ru.rainman.data.local.entity.UserEntity
import ru.rainman.data.remote.api.PostApi
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class PostsRemoteMediator @Inject constructor(
    private val postApi: PostApi,
    private val remoteKeyDao: RemoteKeyDao,
    private val usersSyncUtil: UsersSyncUtil,
    private val appDb: AppDb,
    private val userDao: UserDao,
    private val postSyncUtil: PostSyncUtil
) : RemoteMediator<Int, PostWithUsers>() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var pageIdsRange: LongRange? = null

    init {
        scope.launch {
            remoteKeyDao.insert(RemoteKeysEntity(POSTS_REMOTE_KEYS, null, null))
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostWithUsers>
    ): MediatorResult {

        val response = try {
            when (loadType.log()) {
                REFRESH ->
                    remoteKeyDao.getMax(POSTS_REMOTE_KEYS)
                        ?.let {
                            apiRequest { postApi.getAfter(it, state.config.initialLoadSize) }
                                .plus(
                                    apiRequest {
                                        postApi.getBefore(
                                            it + 1,
                                            state.config.initialLoadSize
                                        )
                                    }.also {
                                        it.lastOrNull()?.let { last ->
                                            it.firstOrNull()?.let { first ->
                                                pageIdsRange = last.id..first.id
                                            }
                                        }
                                    }
                                )
                        } ?: apiRequest { postApi.getLatest(state.config.initialLoadSize) }

                PREPEND -> remoteKeyDao.getMax(POSTS_REMOTE_KEYS)
                    ?.let {
                        apiRequest { postApi.getAfter(it, state.config.initialLoadSize) }
                            .also {
                                it.lastOrNull()?.let { last ->
                                    it.firstOrNull()?.let { first ->
                                        pageIdsRange = last.id..first.id
                                    }
                                }
                            }
                    }
                    ?: return MediatorResult.Success(false)

                APPEND -> remoteKeyDao.getMin(POSTS_REMOTE_KEYS)
                    ?.let {
                        apiRequest { postApi.getBefore(it, state.config.initialLoadSize) }
                            .also {
                                it.lastOrNull()?.let { last ->
                                    it.firstOrNull()?.let { first ->
                                        pageIdsRange = last.id..first.id
                                    }
                                }
                            }
                    }
                    ?: return MediatorResult.Success(false)
            }
        } catch (e: Exception) {
            log(e.message)
            pageIdsRange = null
            return MediatorResult.Error(e)
        }

        if (response.isEmpty()) {
            pageIdsRange = null
            log("empty response")
            return MediatorResult.Success(true)
        }

        val maxId = response.first().id
        val minId = response.last().id

        val users = response.map {
            it.users.map { entry ->
                UserEntity(
                    entry.key,
                    entry.value.name,
                    entry.value.avatar
                )
            }.plus(
                UserEntity(
                    it.authorId,
                    it.author,
                    it.authorAvatar
                )
            )
        }.flatten().toSet()

        userDao.upsert(users.toList())

        scope.launch {
            withContext(coroutineContext) {
                usersSyncUtil.sync(users)
            }
        }

        appDb.withTransaction {
            when (loadType) {
                REFRESH -> {
                    remoteKeyDao.setMin(POSTS_REMOTE_KEYS, minId)
                    remoteKeyDao.setMax(POSTS_REMOTE_KEYS, maxId)
                }

                PREPEND -> remoteKeyDao.setMax(POSTS_REMOTE_KEYS, maxId)
                APPEND -> remoteKeyDao.setMin(POSTS_REMOTE_KEYS, minId)

            }

            postSyncUtil.sync(response, pageIdsRange)
        }

        CoroutineScope(Dispatchers.IO).launch {

//            withContext(coroutineContext) {
//                response
//                    .filter { it.link != null && it.link.startsWith("http") }
//                    .forEach {
//                        withContext(coroutineContext) {
//                            postDao.insertLinkPreview(
//                                it.id,
//                                LinkPreviewBuilder.poll(it.link!!).toEntity()
//                            )
//                        }
//                    }
//            }
        }

        pageIdsRange = null
        return MediatorResult.Success(response.isEmpty())
    }
}

