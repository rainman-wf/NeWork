package ru.rainman.domain.model

data class User(
    override val id: Long,
    val name: String,
    val avatar: String?,
    val jobs: List<Job> = listOf(),
): BaseModel {

    val currentJob: Job? get() = jobs.singleOrNull { it.finish == null }
}
