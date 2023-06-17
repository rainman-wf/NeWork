package ru.rainman.data.impl.post

import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rainman.common.log
import ru.rainman.data.dbQuery
import ru.rainman.data.impl.AttachmentsUtil
import ru.rainman.data.impl.fetchMentioned
import ru.rainman.data.impl.fetchPostLikeOwners
import ru.rainman.data.impl.toEntity
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.PostDao
import ru.rainman.data.local.entity.PostEntity
import ru.rainman.data.local.entity.crossref.PostsLikeOwnersCrossRef
import ru.rainman.data.local.entity.crossref.PostsMentionedUsersCrossRef
import ru.rainman.data.remote.response.PostResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostSyncUtil @Inject constructor(
    private val postDao: PostDao,
    private val db: AppDb,
    private val attachmentsUtil: AttachmentsUtil,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun sync(response: List<PostResponse>, range: LongRange?) {

        val existedPosts = range?.let { postDao.getPostsByIds(it.toList()) }

        val responseIds = response.map { it.id }
        val likeOwners = response.fetchPostLikeOwners()
        val mentioned = response.fetchMentioned()

        val newPosts = response.map { it.toEntity() }

        if (existedPosts.isNullOrEmpty()) {
            postDao.upsertPosts(response.map { it.toEntity() }, likeOwners, mentioned)

            response
                .filter { it.attachment != null && it.attachment.url.startsWith("http") }
                .forEach {
                    scope.launch {
                        attachmentsUtil.getAttachmentEntityFrom(it)?.let { entity ->
                            postDao.insertAttachment(
                                it.id,
                                entity
                            )
                        }
                    }
                }
        } else {

            val existedIds = existedPosts.map { it.id }

            log(existedIds)

            val deletablePosts = existedIds.minus(responseIds.toSet())
            val existedLikes = postDao.getPostsLikeOwners(responseIds)
            val existedMentioned = postDao.getPostsMentioned(responseIds)

            val updatablePosts = newPosts.minus(existedPosts).map {
                it.copy(attachmentKey = existedPosts.singleOrNull { entity -> entity.id == it.id }?.attachmentKey)
            }

            log(updatablePosts.map { it.id })

            val deletableLikes = existedLikes.minus(likeOwners.toSet())
            val deletableMentioned = existedMentioned.minus(mentioned.toSet())

            dbQuery {
                db.withTransaction {
                    postDao.delete(deletablePosts)
                    postDao.deleteLikeOwners(deletableLikes)
                    postDao.deleteMentioned(deletableMentioned)

                    postDao.upsertPosts(updatablePosts, likeOwners, mentioned)
                }
            }

            response.filter { it.attachment != null && existedIds.contains(it.id) }.forEach {
                scope.launch {
                    syncAttachment(it, dbQuery { postDao.getPureEntityById(it.id)!! }.log())
                }
            }

        }
    }

    suspend fun sync(response: PostResponse) {

        val existedPost = postDao.getById(response.id)

        val likeOwners = response.likeOwnerIds.map { PostsLikeOwnersCrossRef(response.id, it) }
        val mentioned = response.mentionIds.map { PostsMentionedUsersCrossRef(response.id, it) }

        if (existedPost == null) {
            postDao.upsertPosts(listOf(response.toEntity()), likeOwners, mentioned)

            scope.launch {
                attachmentsUtil.getAttachmentEntityFrom(response)?.let { entity ->
                    postDao.insertAttachment(
                        response.id,
                        entity
                    )
                }
            }
        } else {

            val existedLikes = postDao.getPostLikeOwners(response.id)
            val existedMentioned = postDao.getPostMentioned(response.id)

            val deletableLikes = existedLikes.minus(likeOwners.toSet())
            val deletableMentioned = existedMentioned.minus(mentioned.toSet())

            dbQuery {
                db.withTransaction {
                    postDao.deleteLikeOwners(deletableLikes)
                    postDao.deleteMentioned(deletableMentioned)
                    postDao.upsertPosts(
                        listOf(
                            response.toEntity().copy(attachmentKey = existedPost.attachment?.key)
                        ), likeOwners, mentioned
                    )
                }
            }

            scope.launch {
                syncAttachment(response, dbQuery { postDao.getPureEntityById(response.id)!! }.log())
            }
        }
    }


    private suspend fun syncAttachment(postResponse: PostResponse, entity: PostEntity) {

        val isAttachmentCorrect = postResponse.attachment?.url?.startsWith("http") == true
        val attachmentKey = entity.attachmentKey

        when {
            postResponse.attachment == null -> attachmentKey?.let { postDao.deleteAttachment(it) }

            !isAttachmentCorrect -> attachmentKey?.let { postDao.deleteAttachment(it) }

            else ->
                if (attachmentKey != null) {
                    log("attachmentKey $attachmentKey")
                    if (postDao.getAttachmentUrl(attachmentKey) != postResponse.attachment.url) {
                        log("replace attachment")
                        postDao.deleteAttachment(attachmentKey)
                        attachmentsUtil.getAttachmentEntityFrom(postResponse)?.let {
                            postDao.insertAttachment(
                                postResponse.id,
                                it
                            )
                        }
                    }
                } else {
                    attachmentsUtil.getAttachmentEntityFrom(postResponse)?.let {
                        log("insert new attachment")
                        postDao.insertAttachment(
                            postResponse.id,
                            it
                        )
                    }
                }
        }
    }
}