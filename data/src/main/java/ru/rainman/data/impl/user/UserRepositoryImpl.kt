package ru.rainman.data.impl.user

import androidx.paging.PagingData
import com.example.common_utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.rainman.data.impl.toEntity
import ru.rainman.data.impl.toModel
import ru.rainman.data.local.dao.JobDao
import ru.rainman.data.local.dao.UserDao
import ru.rainman.data.local.entity.FavoriteUserIdEntity
import ru.rainman.data.local.pref.AppAuth
import ru.rainman.data.remote.api.UserApi
import ru.rainman.data.remote.apiRequest
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

    private val _authError = MutableSharedFlow<ApiError>()
    override val authError: Flow<ApiError> get() = _authError
    override val flowableUsers: Flow<List<User>> = userDao.getAll().map { list ->
        log(list)
        list.map { it.toModel() }
    }

    override suspend fun login(username: String, password: String) {

        try {
            val token = apiRequest { userApi.singIn(AuthenticationRequest(username, password)) }
            appAuth.putAuth(token)
        } catch (e: ApiError) {
            _authError.emit(e)
        }
    }

    override suspend fun create(newObjectDto: NewUserDto): User? {
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
        return withContext(repositoryScope.coroutineContext + Dispatchers.IO) {
            userDao.insert(apiRequest { userApi.getById(token.id) }.toEntity())
            userDao.getById(token.id)?.toModel()
        }
    }

    override suspend fun getById(id: Long): User? {
        return userDao.getById(id)?.toModel()
            ?: withContext(repositoryScope.coroutineContext + Dispatchers.IO) {
                userDao.insert(apiRequest { userApi.getById(id) }.toEntity())
                userDao.getById(id)?.toModel()
            }
    }

    override suspend fun getJobsById(userId: Long): List<Job> {
        return jobDao.getJobByUserId(userId).map { it.toModel() }
    }

    override suspend fun getByIds(ids: List<Long>): List<User> {
        return userDao.getByIds(ids).map { it.toModel() }
    }

    override suspend fun setFavorite(userId: Long, value: Boolean) {
        if (value) userDao.resetFavorite(FavoriteUserIdEntity(userId))
        else userDao.setFavorite(FavoriteUserIdEntity(userId))
    }

    override suspend fun logOut() {
        appAuth.removeAuth()
    }


}

