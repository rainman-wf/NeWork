package ru.rainman.data.remote.response

data class JobResponse (
    val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?
)