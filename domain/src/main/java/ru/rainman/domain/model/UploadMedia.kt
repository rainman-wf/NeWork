package ru.rainman.domain.model

data class UploadMedia(
    val bytes: List<Byte>,
    val fileName: String
) : MediaSource

data class RemoteMedia(
    val url: String
) : MediaSource

sealed interface MediaSource