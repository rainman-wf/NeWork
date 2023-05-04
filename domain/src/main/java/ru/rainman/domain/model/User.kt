package ru.rainman.domain.model

data class User(
    override val id: Long,
    val login: String,
    val name: String,
    val avatar: String?,
    val currentJob: Job? = null,
    val favorite: Boolean
): BaseModel
