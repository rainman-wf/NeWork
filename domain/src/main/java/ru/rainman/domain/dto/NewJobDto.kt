package ru.rainman.domain.dto

import java.time.LocalDate

data class NewJobDto(
    val id: Long,
    val ownerId: Long,
    val name: String,
    val position: String,
    val start: LocalDate,
    val finish: LocalDate? = null,
    val link: String? = null
): NewObjectDto