package ru.rainman.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.rainman.data.local.entity.AttachmentEntity
import ru.rainman.data.local.entity.LinkPreviewEntity
import ru.rainman.data.local.entity.PostEntity
import ru.rainman.data.local.entity.PostWithUsers
import ru.rainman.data.local.entity.crossref.PostsLikeOwnersCrossRef
import ru.rainman.data.local.entity.crossref.PostsMentionedUsersCrossRef
import ru.rainman.data.local.utils.PublicationUsersDiff



@Dao
interface PostDao : BaseDao<PostEntity> {

    @Upsert
    suspend fun upsertAttachment(attachment: AttachmentEntity) : Long

    @Query("UPDATE posts SET attachment_key = :attachmentKey WHERE post_id = :postId")
    suspend fun setAttachment(attachmentKey: Long, postId: Long)

    @Transaction
    suspend fun insertAttachment(postId: Long, attachment: AttachmentEntity) {
        val generatedKey = upsertAttachment(attachment)
        setAttachment(generatedKey, postId)
    }

    @Query("UPDATE posts SET liked_by_me = 1 WHERE post_id = :id")
    suspend fun like(id: Long)

    @Query("SELECT url FROM attachments WHERE `key` = :key")
    suspend fun getAttachmentUrl(key: Long) : String?

    @Query("DELETE FROM attachments WHERE `key` = :key")
    suspend fun deleteAttachment(key: Long)

    @Upsert
    suspend fun upsertLinkPreview(link: LinkPreviewEntity) : Long

    @Query("UPDATE posts SET link_key = :linkKey WHERE post_id = :postId")
    suspend fun setLink(linkKey: Long, postId: Long)

    @Transaction
    suspend fun insertLinkPreview(postId: Long, link: LinkPreviewEntity) {
        val generatedKey = upsertLinkPreview(link)
        setLink(generatedKey, postId)
    }

    @Transaction
    @Upsert
    suspend fun upsertPosts(posts: List<PostEntity>, likeOwners: List<PostsLikeOwnersCrossRef>, mentioned: List<PostsMentionedUsersCrossRef>)

    @Transaction
    @Query("SELECT * FROM posts ORDER BY post_id DESC")
    fun getPaged(): PagingSource<Int, PostWithUsers>

    @Transaction
    @Query("SELECT * FROM posts WHERE author_id = :userId ORDER BY post_id DESC")
    fun getPagedWall(userId: Long) : PagingSource<Int, PostWithUsers>

    @Query("SELECT * FROM posts WHERE post_id = :postId")
    suspend fun getById(postId: Long): PostWithUsers?

    @Query("SELECT * FROM posts WHERE post_id = :postId")
    suspend fun getPureEntityById(postId: Long): PostEntity?

    @Query("SELECT post_id FROM posts WHERE post_id IN(:postId)")
    suspend fun getPostIdsInRange(postId: List<Long>): List<Long>

    @Query("SELECT * FROM posts WHERE post_id IN (:postIds)")
    suspend fun getPostsByIds(postIds: List<Long>) : List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLikeOwners(list: List<PostsLikeOwnersCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMentioned(list: List<PostsMentionedUsersCrossRef>)

    @Delete
    fun deleteLikeOwners(list: List<PostsLikeOwnersCrossRef>)

    @Delete
    fun deleteMentioned(list: List<PostsMentionedUsersCrossRef>)

    @Query("DELETE FROM posts WHERE post_id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM posts WHERE post_id IN(:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("SELECT * FROM posts_like_owners_cross_refs WHERE post_id IN(:postIds)")
    suspend fun getPostsLikeOwners(postIds: List<Long>) : List<PostsLikeOwnersCrossRef>

    @Query("SELECT * FROM posts_like_owners_cross_refs WHERE post_id = :postId")
    suspend fun getPostLikeOwners(postId: Long) : List<PostsLikeOwnersCrossRef>

    @Query("SELECT * FROM posts_mentioned_users_cross_refs WHERE post_id IN(:postIds)")
    suspend fun getPostsMentioned(postIds: List<Long>) : List<PostsMentionedUsersCrossRef>

    @Query("SELECT * FROM posts_mentioned_users_cross_refs WHERE post_id = :postId")
    suspend fun getPostMentioned(postId: Long) : List<PostsMentionedUsersCrossRef>
}