package ru.rainman.data.impl.event

import android.media.MediaMetadataRetriever
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.rainman.data.hasCorrectLink
import ru.rainman.data.impl.AttachmentsUtil
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
import ru.rainman.data.local.entity.AttachmentEntity
import ru.rainman.data.local.entity.AttachmentType
import ru.rainman.data.local.entity.AttachmentType.*
import ru.rainman.data.local.entity.EventAttachmentEntity
import ru.rainman.data.local.entity.EventWithUsers
import ru.rainman.data.local.entity.RemoteKeysEntity
import ru.rainman.data.remote.api.EventApi
import ru.rainman.data.remote.api.UserApi
import ru.rainman.data.remote.apiRequest
import ru.rainman.data.remote.response.Attachment
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
    private val attachmentsUtil: AttachmentsUtil
) : RemoteMediator<Int, EventWithUsers>() {

    private val att = MutableSharedFlow<Pair<Long, Attachment>>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            remoteKeyDao.insert(RemoteKeysEntity(RemoteKeysEntity.Key.EVENTS, null, null))
            att.collect {
                val retriever = MediaMetadataRetriever()
                when (val type = AttachmentType.valueOf(it.second.type)) {
                    IMAGE -> {
                        eventDao.insertAttachment(
                            EventAttachmentEntity(
                                it.first,
                                it.second.url,
                                type,
                                null,
                                null,
                                null,
                                null
                            )
                        )
                    }

                    VIDEO -> {
                        retriever.setDataSource(it.second.url)
                        val duration = attachmentsUtil.getDuration(retriever)
                        val ratio = attachmentsUtil.getVideoRatio(retriever)
                        eventDao.insertAttachment(
                            EventAttachmentEntity(
                                it.first,
                                it.second.url,
                                type,
                                duration,
                                ratio,
                                null,
                                null
                            )
                        )
                    }

                    AUDIO -> {
                        retriever.setDataSource(it.second.url)
                        val duration = attachmentsUtil.getDuration(retriever)
                        val artist = attachmentsUtil.getArtist(retriever)
                        val title = attachmentsUtil.getTitle(retriever)
                        eventDao.insertAttachment(
                            EventAttachmentEntity(
                                it.first,
                                it.second.url,
                                type,
                                duration,
                                null,
                                artist,
                                title
                            )
                        )
                    }
                }
            }
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

        CoroutineScope(Dispatchers.IO).launch {
            response.forEach { event ->
                event.attachment?.let {
                    if (it.url.startsWith("http")) att.emit(Pair(event.id, it))
                }
            }
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

