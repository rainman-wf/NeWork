package ru.rainman.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.rainman.domain.dto.NewUserDto
import ru.rainman.domain.model.Job
import ru.rainman.domain.model.Token
import ru.rainman.domain.model.User

interface UserRepository : BaseRepository<User, NewUserDto> {

    val authToken: Flow<Token?>

    suspend fun login(username: String, password: String)
    suspend fun getJobsById(userId: Long): List<Job>
    suspend fun setFavorite(userId: Long, value: Boolean)
}