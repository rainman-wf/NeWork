package ru.rainman.data.impl

import android.media.MediaMetadataRetriever
import com.example.common_utils.log
import ru.rainman.data.local.entity.AttachmentEntity
import ru.rainman.data.local.entity.AttachmentType
import ru.rainman.data.remote.response.Attachment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentsUtil @Inject constructor() {

    fun getVideoRatio(retriever: MediaMetadataRetriever): Float {
        val weight =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toFloat() ?: 16f
        val height =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toFloat() ?: 9f
        return weight / height
    }

    fun getArtist(retriever: MediaMetadataRetriever): String {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown artist"
    }

    fun getTitle(retriever: MediaMetadataRetriever): String {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "unknown title"
    }

    fun getDuration(retriever: MediaMetadataRetriever): Int {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
    }

}