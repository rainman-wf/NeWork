package ru.rainman.domain.model

data class UploadMedia(
    val bytes: List<Byte>,
    val fileName: String
)