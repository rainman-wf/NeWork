package ru.rainman.data.impl.post

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.common_utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rainman.data.impl.fetchMentioned
import ru.rainman.data.impl.fetchPostLikeOwners
import ru.rainman.data.impl.toEntity
import ru.rainman.data.impl.user.UsersJobsSyncUtil
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.PostDao
import ru.rainman.data.local.dao.RemoteKeyDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.local.entity.PostWithUsers
import ru.rainman.data.local.entity.RemoteKeysEntity
import ru.rainman.data.remote.api.PostApi
import ru.rainman.data.remote.api.UserApi
import ru.rainman.data.remote.apiRequest
import ru.rainman.data.remote.response.PostResponse
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class PostsRemoteMediator @Inject constructor(
    private val postApi: PostApi,
    private val postDao: PostDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val appDb: AppDb,
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val usersJobsSyncUtil: UsersJobsSyncUtil
) : RemoteMediator<Int, PostWithUsers>() {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            remoteKeyDao.insert(RemoteKeysEntity(RemoteKeysEntity.Key.POSTS, null, null))
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostWithUsers>
    ): MediatorResult {

        val response = try {
            log(loadType)
            when (loadType) {
                REFRESH -> remoteKeyDao.getMax(RemoteKeysEntity.Key.POSTS)
                    ?.let { apiRequest { postApi.getAfter(it, state.config.initialLoadSize) } }
                    ?: apiRequest { postApi.getLatest(state.config.initialLoadSize) }
                PREPEND -> remoteKeyDao.getMax(RemoteKeysEntity.Key.POSTS)
                    ?.let { apiRequest { postApi.getAfter(it, state.config.initialLoadSize) } }
                    ?: return MediatorResult.Success(false)
                APPEND -> remoteKeyDao.getMin(RemoteKeysEntity.Key.POSTS)
                    ?.let { apiRequest { postApi.getBefore(it, state.config.initialLoadSize) } }
                    ?: return MediatorResult.Success(false)
            }
        } catch (e: Exception) {
            log(e.message)
            return MediatorResult.Error(e)
        }

        if (response.isEmpty()) {
            return MediatorResult.Success(false)
        }

        val maxId = response.first().id
        val minId = response.last().id

        insertNewUsersFromResponse(response)
        usersJobsSyncUtil.sync(response.map { it.authorId }.toSet())

        appDb.withTransaction {
            when (loadType) {
                REFRESH -> {
                    remoteKeyDao.setMin(minId, RemoteKeysEntity.Key.POSTS)
                    remoteKeyDao.setMax(maxId, RemoteKeysEntity.Key.POSTS)
                }
                PREPEND -> remoteKeyDao.setMax(maxId, RemoteKeysEntity.Key.POSTS)
                APPEND -> remoteKeyDao.setMin(minId, RemoteKeysEntity.Key.POSTS)
            }

            postDao.batchInsert(
                entity = response.map { it.toEntity() },
                likeOwners = response.fetchPostLikeOwners(),
                mentioned = response.fetchMentioned(),
            )
        }
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