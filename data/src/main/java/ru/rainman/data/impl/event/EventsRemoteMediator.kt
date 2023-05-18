package ru.rainman.data.impl.event

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.common_utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.rainman.data.hasCorrectLink
import ru.rainman.data.impl.LinkPreviewBuilder
import ru.rainman.data.impl.fetchEventLikeOwners
import ru.rainman.data.impl.fetchParticipants
import ru.rainman.data.impl.fetchSpeakers
import ru.rainman.data.impl.toEntity
import ru.rainman.data.impl.toEventLinkEntity
import ru.rainman.data.impl.user.UsersJobsSyncUtil
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.EventDao
import ru.rainman.data.local.dao.RemoteKeyDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.local.entity.EventWithUsers
import ru.rainman.data.local.entity.RemoteKeysEntity
import ru.rainman.data.remote.api.EventApi
import ru.rainman.data.remote.api.UserApi
import ru.rainman.data.remote.apiRequest
import ru.rainman.data.remote.response.EventResponse
import javax.inject.Inject

import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class EventsRemoteMediator @Inject constructor(
    private val eventApi: EventApi,
    private val eventDao: EventDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val appDb: AppDb,
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val usersJobsSyncUtil: UsersJobsSyncUtil,
) : RemoteMediator<Int, EventWithUsers>() {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            remoteKeyDao.insert(RemoteKeysEntity(RemoteKeysEntity.Key.EVENTS, null, null))
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventWithUsers>
    ): MediatorResult {

        val response = try {
            log(loadType)
            when (loadType) {
                REFRESH -> remoteKeyDao.getMax(RemoteKeysEntity.Key.EVENTS)
                    ?.let { apiRequest { eventApi.getAfter(it, state.config.initialLoadSize) } }
                    ?: apiRequest { eventApi.getLatest(state.config.initialLoadSize) }
                PREPEND -> remoteKeyDao.getMax(RemoteKeysEntity.Key.EVENTS)
                    ?.let { apiRequest { eventApi.getAfter(it, state.config.initialLoadSize) } }
                    ?: return MediatorResult.Success(false)
                APPEND -> remoteKeyDao.getMin(RemoteKeysEntity.Key.EVENTS)
                    ?.let { apiRequest { eventApi.getBefore(it, state.config.initialLoadSize) } }
                    ?: return MediatorResult.Success(false)
            }
        } catch (e: Exception) {
            log(e.message)
            return MediatorResult.Error(e)
        }

        if (response.isEmpty()) {
            log("empty response")
            return MediatorResult.Success(false)
        }

        val maxId = response.first().id
        val minId = response.last().id

        insertNewUsersFromResponse(response)
        usersJobsSyncUtil.sync(response.map { it.authorId }.toSet())

        CoroutineScope(Dispatchers.IO).async {
            val linkPreview = response
                .filter { it.hasCorrectLink() }
                .map { LinkPreviewBuilder.poll(it.link!!).toEventLinkEntity(it.id) }
            eventDao.insertLinkPreview(linkPreview)
        }

        appDb.withTransaction {
            when (loadType) {
                REFRESH -> {
                    remoteKeyDao.setMin(minId, RemoteKeysEntity.Key.EVENTS)
                    remoteKeyDao.setMax(maxId, RemoteKeysEntity.Key.EVENTS)
                }
                PREPEND -> remoteKeyDao.setMax(maxId, RemoteKeysEntity.Key.EVENTS)
                APPEND -> remoteKeyDao.setMin(minId, RemoteKeysEntity.Key.EVENTS)
            }

            eventDao.batchInsert(
                entity = response.map { it.toEntity() },
                likeOwners = response.fetchEventLikeOwners(),
                speakers = response.fetchSpeakers(),
                participants = response.fetchParticipants()
            )
        }
        return MediatorResult.Success(response.isEmpty())
    }

    private suspend fun insertNewUsersFromResponse(response: List<EventResponse>) = buildList {
        with(response) {
            addAll(map { it.likeOwnerIds }.flatten())
            addAll(map { it.speakerIds }.flatten())
            addAll(map { it.participantsIds }.flatten())
            addAll(map { it.authorId })
        }
    }
        .minus(userDao.getIds().toSet())
        .map { apiRequest { userApi.getById(it) }.toEntity() }
        .apply { userDao.insert(this) }
}

