package ru.rainman.data.remote.response

data class JobResponse (
    override val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    override val link: String?
) : LinkedResponse