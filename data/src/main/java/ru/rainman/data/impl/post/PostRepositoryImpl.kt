package ru.rainman.data.impl.post


import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.rainman.data.impl.toModel
import ru.rainman.data.local.dao.PostDao
import ru.rainman.data.remote.api.PostApi
import ru.rainman.data.remote.apiRequest
import ru.rainman.domain.dto.NewPostDto
import ru.rainman.domain.model.Post
import ru.rainman.domain.repository.PostRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postsPagedData: PostsPagedData,
    private val postSyncUtil: PostSyncUtil,
    private val postDao: PostDao,
    private val postApi: PostApi
) : PostRepository {

    override val data: Flow<PagingData<Post>> = postsPagedData.data
    override val wall: Flow<PagingData<Post>> = postsPagedData.wall

    override suspend fun defWallOwnerId(userId: Long) {
        postsPagedData.setWallOwnerId(userId)
    }

    override suspend fun create(newObjectDto: NewPostDto): Post? {
        TODO("Not yet implemented")
    }

    override suspend fun getById(id: Long): Post? {
        TODO("Not yet implemented")
    }

    override suspend fun getByIds(ids: List<Long>): List<Post> {
        TODO("Not yet implemented")
    }



    override suspend fun delete(id: Long): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun like(id: Long): Post {
        return withContext(repositoryScope.coroutineContext) {
            val likedByMe = postDao.likedByMe(id)
            val event = apiRequest {
                if (!likedByMe) postApi.like(id)
                else postApi.unlike(id)
            }
            postSyncUtil.sync(event).toModel()
        }
    }
}