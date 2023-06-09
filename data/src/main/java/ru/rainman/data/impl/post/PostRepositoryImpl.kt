package ru.rainman.data.impl.post


import androidx.paging.PagingData
import androidx.room.withTransaction
import com.example.common_utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.rainman.data.impl.AttachmentsUtil
import ru.rainman.data.impl.toEntity
import ru.rainman.data.impl.toModel
import ru.rainman.data.impl.toRequestBody
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.PostDao
import ru.rainman.data.local.entity.PostAttachmentEntity
import ru.rainman.data.remote.api.MediaApi
import ru.rainman.data.remote.api.PostApi
import ru.rainman.data.remote.apiRequest
import ru.rainman.data.remote.response.Attachment
import ru.rainman.domain.dto.NewPostDto
import ru.rainman.domain.model.Post
import ru.rainman.domain.model.RemoteMedia
import ru.rainman.domain.model.UploadMedia
import ru.rainman.domain.repository.PostRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postsPagedData: PostsPagedData,
    private val postSyncUtil: PostSyncUtil,
    private val postDao: PostDao,
    private val postApi: PostApi,
    private val mediaApi: MediaApi,
    private val attachmentsUtil: AttachmentsUtil,
    private val db: AppDb
) : PostRepository {

    override val data: Flow<PagingData<Post>> = postsPagedData.data
    override val wall: Flow<PagingData<Post>> = postsPagedData.wall

    override suspend fun defWallOwnerId(userId: Long) {
        postsPagedData.setWallOwnerId(userId)
    }

    override suspend fun create(newObjectDto: NewPostDto): Post? {
        val attachment = newObjectDto.attachment?.let { dto ->

            attachmentsUtil.dtoToAttachment(dto) {
                apiRequest {
                    mediaApi.uploadMedia(
                        (dto.media as UploadMedia).bytes.toByteArray()
                            .toRequestBody("multipart/from-data".toMediaType())
                            .let { body ->
                                MultipartBody.Part.createFormData(
                                    "file",
                                    (dto.media as UploadMedia).fileName,
                                    body
                                )
                            }
                    )
                }
            }
        }

        return withContext(repositoryScope.coroutineContext + Dispatchers.IO) {
            val post = try {
                apiRequest { postApi.create(newObjectDto.toRequestBody(attachment)) }.log()
            } catch (e: Exception) {
                throw e
            }
            try {
                db.withTransaction {
                    postDao.upsert(post.toEntity())
                    post.attachment?.let {
                        postDao.insertAttachment(
                            attachmentsUtil.getAttachmentEntityFrom(
                                post.id,
                                it
                            ) as PostAttachmentEntity
                        )
                    }
                }
            } catch (e: IOException) {
                throw e
            }
            postDao.getById(post.id)?.toModel()
        }
    }

    override suspend fun getById(id: Long): Post? {
        return postDao.getById(id)?.toModel()
    }

    override suspend fun getByIds(ids: List<Long>): List<Post> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Long) {
        withContext(repositoryScope.coroutineContext) {
            try {
                apiRequest { postApi.delete(id) }
                postDao.delete(id)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun like(id: Long): Post {
        return withContext(repositoryScope.coroutineContext) {
            val likedByMe = postDao.likedByMe(id)
            val post = apiRequest {
                if (!likedByMe) postApi.like(id)
                else postApi.unlike(id)
            }
            postSyncUtil.sync(post).toModel()
        }
    }
}