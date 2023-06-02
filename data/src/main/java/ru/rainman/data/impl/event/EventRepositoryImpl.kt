package ru.rainman.data.impl.event

import androidx.paging.PagingData
import com.example.common_utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.rainman.data.impl.toEntity
import ru.rainman.data.impl.toModel
import ru.rainman.data.impl.toRequestBody
import ru.rainman.data.local.dao.EventDao
import ru.rainman.data.remote.api.EventApi
import ru.rainman.data.remote.api.MediaApi
import ru.rainman.data.remote.apiRequest
import ru.rainman.data.remote.response.Attachment
import ru.rainman.domain.dto.NewEventDto
import ru.rainman.domain.model.Event
import ru.rainman.domain.repository.EventRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    eventsPagedData: EventsPagedData,
    private val eventDao: EventDao,
    private val eventApi: EventApi,
    private val eventSyncUtil: EventSyncUtil,
    private val mediaApi: MediaApi
) : EventRepository {

    override val data: Flow<PagingData<Event>> = eventsPagedData.data

    override suspend fun create(newObjectDto: NewEventDto): Event? {

        val attachment = newObjectDto.attachment?.let { dto ->
            val media = try {
                apiRequest {
                    mediaApi.uploadMedia(
                        dto.media.bytes.toByteArray()
                            .toRequestBody("multipart/from-data".toMediaType()).let { body ->
                                MultipartBody.Part.createFormData(
                                    "file",
                                    dto.media.fileName,
                                    body
                                )
                            }
                    )
                }
            } catch (e: Exception) {
                log(e.message)
                return null
            }

            Attachment(dto.type.name, media.url)
        }

        log(newObjectDto.dateTime)

        return withContext(repositoryScope.coroutineContext + Dispatchers.IO) {
            val event = try {
                apiRequest { eventApi.create(newObjectDto.toRequestBody(attachment)) }
            } catch (e: Exception) {
                log (e)
                return@withContext null
            }
            eventDao.insert(event.toEntity())
            eventDao.getById(event.id).toModel()
        }
    }

    override suspend fun getById(id: Long): Event? {
        TODO("Not yet implemented")
    }

    override suspend fun getByIds(ids: List<Long>): List<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Long): Boolean {
        TODO("Not yet implemented")
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
