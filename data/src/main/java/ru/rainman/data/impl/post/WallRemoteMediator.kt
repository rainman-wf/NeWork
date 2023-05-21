package ru.rainman.data.impl.post

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.common_utils.log
import ru.rainman.data.local.entity.PostWithUsers
import ru.rainman.data.impl.*
import ru.rainman.data.impl.user.UsersJobsSyncUtil
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.PostDao
import ru.rainman.data.local.dao.RemoteKeyDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.local.entity.WallRemoteKeyEntity
import ru.rainman.data.remote.api.UserApi
import ru.rainman.data.remote.api.WallApi
import ru.rainman.data.remote.apiRequest
import ru.rainman.data.remote.response.PostResponse
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class WallRemoteMediator @Inject constructor(
    private val wallApi: WallApi,
    private val postDao: PostDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val appDb: AppDb,
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val usersJobsSyncUtil: UsersJobsSyncUtil
) : RemoteMediator<Int, PostWithUsers>() {

    var wallOwnerId: Long = 0
        set(value) {
            log(value)
            if (value > 0L) remoteKeyDao.insert(WallRemoteKeyEntity(value))
            field = value
        }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostWithUsers>
    ): MediatorResult {

        val response = try {
            log(loadType)
            when (loadType) {
                REFRESH -> remoteKeyDao.getMax(wallOwnerId)
                    ?.let {
                        apiRequest {
                            wallApi.getAfter(wallOwnerId, it, state.config.initialLoadSize)
                        }
                    } ?: apiRequest { wallApi.getLatest(wallOwnerId, state.config.initialLoadSize) }
                PREPEND -> remoteKeyDao.getMax(wallOwnerId)
                    ?.let {
                        apiRequest {
                            wallApi.getAfter(wallOwnerId, it, state.config.initialLoadSize)
                        }
                    } ?: return MediatorResult.Success(false)
                APPEND -> remoteKeyDao.getMin(wallOwnerId)
                    ?.let {
                        apiRequest {
                            wallApi.getBefore(wallOwnerId, it, state.config.initialLoadSize)
                        }
                    } ?: return MediatorResult.Success(false)
            }
        } catch (e: Exception) {
            log(e.message)
            return MediatorResult.Error(e)
        }

        if (response.isEmpty()) {
            log("empty response")
            return MediatorResult.Success(true)
        }

        val maxId = response.first().id
        val minId = response.last().id

        log ("ids max = $maxId and min = $minId")

        insertNewUsersFromResponse(response)
        usersJobsSyncUtil.sync(response.map { it.authorId }.toSet())

        log("user jobs synced")

        appDb.withTransaction {
            when (loadType) {
                REFRESH -> {
                    remoteKeyDao.setMin(minId, wallOwnerId)
                    remoteKeyDao.setMax(maxId, wallOwnerId)
                }
                PREPEND -> remoteKeyDao.setMax(maxId, wallOwnerId)
                APPEND -> remoteKeyDao.setMin(minId, wallOwnerId)
            }

            postDao.batchInsert(
                entity = response.map { it.toEntity() },
                likeOwners = response.fetchPostLikeOwners(),
                mentioned = response.fetchMentioned(),
            )
        }
        log("block loading cause response is empty")
        return MediatorResult.Success(response.isEmpty())
    }


    private suspend fun insertNewUsersFromResponse(response: List<PostResponse>) = buildList {
        with(response) {
            addAll(map { it.likeOwnerIds }.flatten())
            addAll(map { it.mentionIds }.flatten())
            addAll(map { it.authorId })
        }
    }
        .minus(userDao.getIds().toSet())
        .map { apiRequest { userApi.getById(it) }.toEntity() }
        .apply { userDao.insert(this) }
}