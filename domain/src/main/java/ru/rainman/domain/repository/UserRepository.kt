package ru.rainman.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.rainman.domain.dto.NewUserDto
import ru.rainman.domain.model.ApiError
import ru.rainman.domain.model.AppError
import ru.rainman.domain.model.Job
import ru.rainman.domain.model.Token
import ru.rainman.domain.model.User

interface UserRepository : BaseRepository<User, NewUserDto> {

    val authToken: Flow<Token?>
    val flowableUsers: Flow<List<User>>

    suspend fun login(username: String, password: String)
    suspend fun logOut()
    suspend fun getJob(id: Long) : Job?
}