package ru.rainman.data.impl.event

import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.rainman.data.apiRequest
import ru.rainman.data.dbQuery
import ru.rainman.data.impl.AttachmentsUtil
import ru.rainman.data.impl.toModel
import ru.rainman.data.impl.toRequestBody
import ru.rainman.data.local.dao.EventDao
import ru.rainman.data.remote.api.EventApi
import ru.rainman.data.remote.api.MediaApi
import ru.rainman.domain.dto.NewEventDto
import ru.rainman.domain.model.Event
import ru.rainman.domain.model.UploadMedia
import ru.rainman.domain.repository.EventRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    eventsPagedData: EventsPagedData,
    private val eventDao: EventDao,
    private val eventApi: EventApi,
    private val eventSyncUtil: EventSyncUtil,
    private val mediaApi: MediaApi,
    private val attachmentsUtil: AttachmentsUtil,
) : EventRepository {

    override val data: Flow<PagingData<Event>> = eventsPagedData.data

    override suspend fun create(newObjectDto: NewEventDto) {

        val attachment = newObjectDto.attachment?.let { dto ->

            attachmentsUtil.dtoToAttachment(dto) {
                apiRequest {
                    mediaApi.uploadMedia(
                        (dto.media as UploadMedia).bytes.toByteArray()
                            .toRequestBody("multipart/from-data".toMediaType())
                            .let { body ->
                                MultipartBody.Part.createFormData(
                                    "file",
                                    (dto.media as UploadMedia).fileName,
                                    body
                                )
                            }
                    )
                }
            }
        }

        withContext(repositoryScope.coroutineContext + Dispatchers.IO) {
            val event =
                listOf(apiRequest { eventApi.create(newObjectDto.toRequestBody(attachment)) })

            eventSyncUtil.sync(event, null)

        }
    }

    override suspend fun getById(id: Long): Event? {
        return eventDao.getById(id)?.toModel()
    }

    override suspend fun getByIds(ids: List<Long>): List<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Long) {
        withContext(repositoryScope.coroutineContext) {
            apiRequest { eventApi.delete(id) }
            dbQuery { eventDao.delete(id) }
        }
    }

    override suspend fun like(id: Long) {
        withContext(repositoryScope.coroutineContext) {
            val event = listOf(apiRequest { eventApi.like(id) })
            eventSyncUtil.sync(event, null)
        }
    }

    override suspend fun unlike(id: Long) {
        withContext(repositoryScope.coroutineContext) {
            eventSyncUtil.sync(apiRequest { eventApi.like(id) })
        }
    }

    override suspend fun participate(id: Long) {
        withContext(repositoryScope.coroutineContext) {
            eventSyncUtil.sync(apiRequest { eventApi.crateParticipant(id) })
        }
    }

    override suspend fun leave(id: Long) {
        return withContext(repositoryScope.coroutineContext) {
            eventSyncUtil.sync(apiRequest { eventApi.deleteParticipant(id) })
        }
    }

}
