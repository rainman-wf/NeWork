package ru.rainman.data.impl

import android.media.MediaMetadataRetriever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.rainman.data.local.entity.AttachmentEntity
import ru.rainman.data.local.entity.AttachmentType
import ru.rainman.data.local.entity.PostAttachmentEntity
import ru.rainman.data.remote.apiRequest
import ru.rainman.data.remote.response.Attachment
import ru.rainman.data.remote.response.MediaResponse
import ru.rainman.domain.dto.NewAttachmentDto
import ru.rainman.domain.model.RemoteMedia
import ru.rainman.domain.model.UploadMedia
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class AttachmentsUtil @Inject constructor() {

    private val converter = MutableSharedFlow<AttachmentConverterPrepareDto>()

    private data class AttachmentConverterPrepareDto(
        val id: Long,
        val attachment: Attachment,
        val result: (AttachmentEntity) -> Unit
    )

    suspend fun dtoToAttachment(
        dto: NewAttachmentDto,
        block: suspend () -> MediaResponse
    ): Attachment {

        val url = when (dto.media) {
            is RemoteMedia -> (dto.media as RemoteMedia).url
            is UploadMedia -> try {
                block().url
            } catch (e: Exception) {
                throw e
            }
        }
        return Attachment(
            type = dto.type.name,
            url = url
        )
    }

    fun getVideoRatio(retriever: MediaMetadataRetriever): Float {
        val weight =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toFloat()
                ?: 16f
        val height =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toFloat()
                ?: 9f
        return weight / height
    }

    fun getArtist(retriever: MediaMetadataRetriever): String {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            ?: "Unknown artist"
    }

    fun getTitle(retriever: MediaMetadataRetriever): String {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            ?: "unknown title"
    }

    fun getDuration(retriever: MediaMetadataRetriever): Int {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            converter.collect {
                withContext(this.coroutineContext) {
                    val retriever = MediaMetadataRetriever()
                    val attachment = when (val type = AttachmentType.valueOf(it.attachment.type)) {

                        AttachmentType.IMAGE ->
                            PostAttachmentEntity(
                                it.id,
                                it.attachment.url,
                                type,
                                null,
                                null,
                                null,
                                null
                            )

                        AttachmentType.VIDEO -> {
                            retriever.setDataSource(it.attachment.url)
                            val duration = getDuration(retriever)
                            val ratio = getVideoRatio(retriever)

                            PostAttachmentEntity(
                                it.id,
                                it.attachment.url,
                                type,
                                duration,
                                ratio,
                                null,
                                null
                            )
                        }

                        AttachmentType.AUDIO -> {
                            retriever.setDataSource(it.attachment.url)
                            val duration = getDuration(retriever)
                            val artist = getArtist(retriever)
                            val title = getTitle(retriever)
                            PostAttachmentEntity(
                                it.id,
                                it.attachment.url,
                                type,
                                duration,
                                null,
                                artist,
                                title
                            )
                        }
                    }

                    it.result.invoke(attachment)
                }
            }
        }
    }

    suspend fun getAttachmentEntityFrom(postId: Long, attachment: Attachment): AttachmentEntity =
        suspendCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                converter.emit(
                    AttachmentConverterPrepareDto(postId, attachment) {
                        continuation.resume(it)
                    }
                )
            }
        }

}