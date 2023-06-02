package ru.rainman.domain.model

import java.io.Serializable


sealed class Attachment(open val uri: String) : Serializable {

    data class Image(override val uri: String, val ratio: Float?) : Attachment(uri)
    data class Video(
        override val uri: String,
        val duration: Int,
        val ratio: Float,
    ) : Attachment(uri)

    data class Audio(
        override val uri: String,
        val duration: Int,
        val artist: String,
        val title: String
    ) : Attachment(uri)
}



