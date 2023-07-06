package ru.rainman.data.impl

import android.media.MediaMetadataRetriever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.rainman.common.log
import ru.rainman.data.local.entity.AttachmentEntity
import ru.rainman.data.local.entity.AttachmentType
import ru.rainman.data.remote.response.Attachment
import ru.rainman.data.remote.response.MediaResponse
import ru.rainman.data.remote.response.PublicationResponse
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
        val publicationResponse: PublicationResponse,
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

    private fun getVideoRatio(retriever: MediaMetadataRetriever): Float {
        val weight =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toFloat()
                ?: 16f
        val height =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toFloat()
                ?: 9f
        return weight / height
    }

    private fun getVImageRatio(retriever: MediaMetadataRetriever): Float {
        val weight =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH)?.toFloat()
                ?: 16f
        val height =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH)?.toFloat()
                ?: 9f
        return weight / height
    }

    private fun getArtist(retriever: MediaMetadataRetriever): String {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            ?: "Unknown artist"
    }

    private fun getTitle(retriever: MediaMetadataRetriever): String {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            ?: "unknown title"
    }

    private fun getDuration(retriever: MediaMetadataRetriever): Int {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            converter.collect {

                val attachmentResponse = it.publicationResponse.attachment!!

                var duration: Int? = null
                var ratio: Float? = null
                var artist: String? = null
                var title: String? = null
                val type = AttachmentType.valueOf(attachmentResponse.type)

                CoroutineScope(coroutineContext).launch poll@{

                    val retriever = MediaMetadataRetriever()


                    try {
                        retriever.setDataSource(attachmentResponse.url)
                    } catch (e: RuntimeException) {
                        log(e.message)
                    }

                    when (type) {
                        AttachmentType.IMAGE -> {
                            ratio = getVImageRatio(retriever)
                        }

                        AttachmentType.VIDEO -> {
                            duration = getDuration(retriever)
                            ratio = getVideoRatio(retriever)
                        }

                        AttachmentType.AUDIO -> {
                            duration = getDuration(retriever)
                            artist = getArtist(retriever)
                            title = getTitle(retriever)
                        }
                    }

                    retriever.release()

                    val attachment = AttachmentEntity(
                        0,
                        it.publicationResponse.attachment!!.url,
                        type,
                        duration,
                        ratio,
                        artist,
                        title
                    )


                    it.result.invoke(attachment)
                }
            }
        }
    }

    suspend fun getAttachmentEntityFrom(publicationResponse: PublicationResponse): AttachmentEntity? =
        suspendCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                converter.emit(
                    AttachmentConverterPrepareDto(publicationResponse) {
                        continuation.resume(it)
                    }
                )
            }
        }
}