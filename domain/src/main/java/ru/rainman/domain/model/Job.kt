package ru.rainman.domain.model

import java.time.LocalDateTime

data class Job(
    override val id: Long,
    val name: String,
    val position: String,
    val start: LocalDateTime,
    val finish: LocalDateTime?,
    val link: String?
) : BaseModel
