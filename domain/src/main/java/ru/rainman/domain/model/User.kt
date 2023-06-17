package ru.rainman.domain.model

data class User(
    override val id: Long,
    val name: String,
    val avatar: String?,
    val jobs: List<Job> = listOf(),
    val favorite: Boolean,
): BaseModel {

    val currentJob: Job? = jobs.singleOrNull { it.finish == null }
}
