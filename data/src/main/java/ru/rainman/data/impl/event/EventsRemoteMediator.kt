package ru.rainman.data.impl.event

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rainman.common.EVENTS_REMOTE_KEYS
import ru.rainman.common.POSTS_REMOTE_KEYS
import ru.rainman.common.log
import ru.rainman.data.apiRequest
import ru.rainman.data.impl.user.UsersSyncUtil
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.RemoteKeyDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.local.entity.AttachmentType.*
import ru.rainman.data.local.entity.EventWithUsers
import ru.rainman.data.local.entity.RemoteKeysEntity
import ru.rainman.data.local.entity.UserEntity
import ru.rainman.data.remote.api.EventApi
import javax.inject.Inject

import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalPagingApi::class)
@Singleton
class EventsRemoteMediator @Inject constructor(
    private val eventApi: EventApi,
    private val eventSyncUtil: EventSyncUtil,
    private val remoteKeyDao: RemoteKeyDao,
    private val appDb: AppDb,
    private val userDao: UserDao,
    private val usersSyncUtil: UsersSyncUtil,
) : RemoteMediator<Int, EventWithUsers>() {


    private val scope = CoroutineScope(Dispatchers.IO)
    private var pageIdsRange: LongRange? = null

    init {
        scope.launch {
            remoteKeyDao.insert(RemoteKeysEntity(EVENTS_REMOTE_KEYS, null, null))
        }
    }


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventWithUsers>
    ): MediatorResult {

        val response = try {
            when (loadType) {
                REFRESH ->
                    remoteKeyDao.getMax(EVENTS_REMOTE_KEYS)
                        ?.let { apiRequest { eventApi.getAfter(it, state.config.initialLoadSize) } }
                        ?: apiRequest { eventApi.getLatest(state.config.initialLoadSize) }

                PREPEND -> remoteKeyDao.getMax(EVENTS_REMOTE_KEYS)
                    ?.let {
                        pageIdsRange = (it..it + state.config.initialLoadSize)
                        apiRequest { eventApi.getAfter(it, state.config.initialLoadSize) }
                    }
                    ?: return MediatorResult.Success(false)

                APPEND -> remoteKeyDao.getMin(EVENTS_REMOTE_KEYS)
                    ?.let {
                        pageIdsRange = (it - state.config.initialLoadSize.toLong()..it)
                        apiRequest { eventApi.getBefore(it, state.config.initialLoadSize) }
                    }
                    ?: return MediatorResult.Success(false)
            }
        } catch (e: Exception) {
            log(e.message)
            pageIdsRange = null
            return MediatorResult.Error(e)
        }

        pageIdsRange?.log()

        if (response.isEmpty()) {
            pageIdsRange = null
            return MediatorResult.Success(false)
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
                    remoteKeyDao.setMin(EVENTS_REMOTE_KEYS, minId)
                    remoteKeyDao.setMax(EVENTS_REMOTE_KEYS, maxId)
                }

                PREPEND -> remoteKeyDao.setMax(EVENTS_REMOTE_KEYS, maxId)
                APPEND -> remoteKeyDao.setMin(EVENTS_REMOTE_KEYS, minId)
            }

            eventSyncUtil.sync(response, pageIdsRange)
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

        return MediatorResult.Success(response.isEmpty())
    }

}

