package ru.rainman.data.impl.post

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
import ru.rainman.common.log
import ru.rainman.data.apiRequest
import ru.rainman.data.local.entity.PostWithUsers
import ru.rainman.data.impl.*
import ru.rainman.data.impl.user.UsersSyncUtil
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.RemoteKeyDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.local.entity.RemoteKeysEntity
import ru.rainman.data.local.entity.UserEntity
import ru.rainman.data.remote.api.WallApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalPagingApi::class)
@Singleton
class WallRemoteMediator @Inject constructor(
    private val wallApi: WallApi,
    private val remoteKeyDao: RemoteKeyDao,
    private val appDb: AppDb,
    private val userDao: UserDao,
    private val usersSyncUtil: UsersSyncUtil,
    private val postSyncUtil: PostSyncUtil
) : RemoteMediator<Int, PostWithUsers>() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var pageIdsRange: LongRange? = null

    var wallOwnerId: Long = 0
        set(value) {
            if (value > 0L) CoroutineScope(Dispatchers.IO).launch {
                remoteKeyDao.insert(RemoteKeysEntity(value, null, null))
            }
            field = value
        }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostWithUsers>
    ): MediatorResult {

        val response = try {
            when (loadType) {
                REFRESH ->
                    remoteKeyDao.getMax(wallOwnerId)
                        ?.let { apiRequest { wallApi.getAfter(wallOwnerId, it, state.config.initialLoadSize) } }
                        ?: apiRequest { wallApi.getLatest(wallOwnerId, state.config.initialLoadSize) }

                PREPEND -> remoteKeyDao.getMax(wallOwnerId)
                    ?.let {
                        pageIdsRange = (it..it + state.config.initialLoadSize)
                        apiRequest { wallApi.getAfter(wallOwnerId, it, state.config.initialLoadSize) }
                    }
                    ?: return MediatorResult.Success(false)

                APPEND -> remoteKeyDao.getMin(wallOwnerId)
                    ?.let {
                        pageIdsRange = (it - state.config.initialLoadSize.toLong()..it)
                        apiRequest { wallApi.getBefore(wallOwnerId, it, state.config.initialLoadSize) }
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
                    remoteKeyDao.setMin(wallOwnerId, minId)
                    remoteKeyDao.setMax(wallOwnerId, maxId)
                }

                PREPEND -> remoteKeyDao.setMax(wallOwnerId, maxId)
                APPEND -> remoteKeyDao.setMin(wallOwnerId, minId)

            }

            postSyncUtil.sync(response, pageIdsRange)
        }

        return MediatorResult.Success(response.isEmpty())
    }

}