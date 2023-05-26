package ru.rainman.domain.model


sealed class Attachment(open val url: String)

sealed class Playable(override val url: String, open val duration: Int) : Attachment(url)

data class Image(override val url: String, val ratio: Float?) : Attachment(url)

data class Video(
    override val url: String,
    override val duration: Int,
    val ratio: Float,
) : Playable(url, duration)

data class Audio(
    override val url: String,
    override val duration: Int,
    val artist: String,
    val title: String
) : Playable(url, duration)

