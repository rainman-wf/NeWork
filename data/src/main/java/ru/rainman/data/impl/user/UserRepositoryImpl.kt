package ru.rainman.data.impl.user

import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.rainman.common.log
import ru.rainman.data.apiRequest
import ru.rainman.data.dbQuery
import ru.rainman.data.impl.toEntity
import ru.rainman.data.impl.toModel
import ru.rainman.data.local.dao.JobDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.local.pref.AppAuth
import ru.rainman.data.remote.api.UserApi
import ru.rainman.data.remote.request.AuthenticationRequest
import ru.rainman.domain.dto.NewUserDto
import ru.rainman.domain.model.ApiError
import ru.rainman.domain.model.Job
import ru.rainman.domain.model.Token
import ru.rainman.domain.model.User
import ru.rainman.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val appAuth: AppAuth,
    private val userDao: UserDao,
    private val jobDao: JobDao,
    usersPagedData: UsersPagedData,
) : UserRepository {

    override val data: Flow<PagingData<User>> = usersPagedData.data
    override val authToken: Flow<Token?> = appAuth.token

    override val flowableUsers: Flow<List<User>> = userDao.getAll().map { list ->
        list.map { it.toModel() }
    }

    override suspend fun login(username: String, password: String) {
        val token = apiRequest { userApi.singIn(AuthenticationRequest(username, password)) }
        appAuth.putAuth(token)
    }

    override suspend fun create(newObjectDto: NewUserDto) {
        val token = apiRequest {
            with(newObjectDto) {
                userApi.register(
                    login = login.toRequestBody("text/plane".toMediaType()),
                    password = password.toRequestBody("text/plane".toMediaType()),
                    name = name.toRequestBody("text/plane".toMediaType()),
                    file = avatar?.let { avatar ->
                        avatar.bytes.toByteArray()
                            .toRequestBody("multipart/from-data".toMediaType()).let {
                            MultipartBody.Part.createFormData(
                                "file",
                                avatar.fileName,
                                it
                            )
                        }
                    }
                )
            }
        }
        appAuth.putAuth(token)
        withContext(repositoryScope.coroutineContext + Dispatchers.IO) {
            dbQuery { userDao.insert(apiRequest { userApi.getById(token.id) }.toEntity()) }
        }
    }

    override suspend fun getById(id: Long): User? {
        return userDao.getById(id.log())?.toModel()
            ?: withContext(repositoryScope.coroutineContext + Dispatchers.IO) {
                userDao.insert(apiRequest { userApi.getById(id) }.toEntity())
                userDao.getById(id)?.toModel()
            }
    }

    override suspend fun getByIds(ids: List<Long>): List<User> {
        return userDao.getByIds(ids).map { it.toModel() }
    }

    override suspend fun logOut() {
        appAuth.removeAuth()
    }

    override suspend fun getJob(id: Long): Job? {
        return dbQuery { jobDao.getById(id)?.toModel() }
    }
}

