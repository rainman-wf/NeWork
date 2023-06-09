package ru.rainman.data.impl.event

import androidx.paging.PagingData
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.rainman.data.impl.AttachmentsUtil
import ru.rainman.data.impl.toEntity
import ru.rainman.data.impl.toModel
import ru.rainman.data.impl.toRequestBody
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.EventDao
import ru.rainman.data.local.entity.EventAttachmentEntity
import ru.rainman.data.remote.api.EventApi
import ru.rainman.data.remote.api.MediaApi
import ru.rainman.data.remote.apiRequest
import ru.rainman.domain.dto.NewEventDto
import ru.rainman.domain.model.Event
import ru.rainman.domain.model.UploadMedia
import ru.rainman.domain.repository.EventRepository
import java.io.IOException
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
    private val db: AppDb
) : EventRepository {

    override val data: Flow<PagingData<Event>> = eventsPagedData.data

    override suspend fun create(newObjectDto: NewEventDto): Event? {

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

        return withContext(repositoryScope.coroutineContext + Dispatchers.IO) {
            val event = try {
                apiRequest { eventApi.create(newObjectDto.toRequestBody(attachment)) }
            } catch (e: Exception) {
                throw e
            }

            try {
                db.withTransaction {
                    eventDao.insert(event.toEntity())

                    event.attachment?.let {
                        eventDao.insertAttachment(
                            attachmentsUtil.getAttachmentEntityFrom(
                                event.id,
                                it
                            ) as EventAttachmentEntity
                        )
                    }
                }
            } catch (e: IOException) {
                throw e
            }
            eventDao.getById(event.id)?.toModel()
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
            try {
                apiRequest { eventApi.delete(id) }
                eventDao.delete(id)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun like(id: Long): Event {
        return withContext(repositoryScope.coroutineContext) {
            val likedByMe = eventDao.likedByMe(id)
            val event = apiRequest {
                if (!likedByMe) eventApi.like(id)
                else eventApi.unlike(id)
            }
            eventSyncUtil.sync(event).toModel()
        }
    }


    override suspend fun participate(id: Long): Event {
        return withContext(repositoryScope.coroutineContext) {
            val participateByMe = eventDao.participatedByMe(id)
            val event = apiRequest {
                if (!participateByMe) eventApi.crateParticipant(id)
                else eventApi.deleteParticipant(id)
            }
            eventSyncUtil.sync(event).toModel()
        }
    }
}
