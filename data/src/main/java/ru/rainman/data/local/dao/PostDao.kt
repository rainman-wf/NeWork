package ru.rainman.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.rainman.data.local.entity.EventAttachmentEntity
import ru.rainman.data.local.entity.PostAttachmentEntity
import ru.rainman.data.local.entity.PostEntity
import ru.rainman.data.local.entity.PostWithUsers
import ru.rainman.data.local.entity.crossref.PostsLikeOwnersCrossRef
import ru.rainman.data.local.entity.crossref.PostsMentionedUsersCrossRef
import ru.rainman.data.local.utils.PublicationUsersDiff

@Dao
interface PostDao : BaseDao<PostEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: PostAttachmentEntity) : Long

    @Transaction
    @Query("SELECT * FROM posts ORDER BY post_id DESC")
    fun getPaged(): PagingSource<Int, PostWithUsers>

    @Transaction
    @Query("SELECT * FROM posts WHERE author_id = :userId ORDER BY post_id DESC")
    fun getPagedWall(userId: Long) : PagingSource<Int, PostWithUsers>

    @Query("SELECT liked_by_me FROM posts WHERE post_id = :postId")
    suspend fun likedByMe(postId: Long) : Boolean

    @Query("SELECT * FROM posts WHERE post_id = :postId")
    suspend fun getById(postId: Long): PostWithUsers?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLikOwners(list: List<PostsLikeOwnersCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMentioned(list: List<PostsMentionedUsersCrossRef>)

    @Transaction
    suspend fun batchInsert(
        entity: List<PostEntity>,
        likeOwners: List<PostsLikeOwnersCrossRef>,
        mentioned: List<PostsMentionedUsersCrossRef>
    ) {
        insert(entity)
        insertLikOwners(likeOwners)
        insertMentioned(mentioned)
    }

    @Query("SELECT * FROM posts_like_owners_cross_refs WHERE post_id IN (:list)")
    suspend fun getPostsLikeOwners(list: List<Long>): List<PostsLikeOwnersCrossRef>

    @Query("SELECT * FROM posts_mentioned_users_cross_refs WHERE post_id IN (:list)")
    suspend fun getPostsMentioned(list: List<Long>): List<PostsMentionedUsersCrossRef>

    @Delete
    fun deleteLikeOwners(list: List<PostsLikeOwnersCrossRef>)

    @Delete
    fun deleteMentioned(list: List<PostsMentionedUsersCrossRef>)

    @Transaction
    suspend fun updateLikeOwners(
        publicationUsersDiff: PublicationUsersDiff<PostsLikeOwnersCrossRef>
    ) {
        insertLikOwners(publicationUsersDiff.toInsert)
        deleteLikeOwners(publicationUsersDiff.toDelete)
    }

    @Transaction
    suspend fun updateMentioned(
        publicationUsersDiff: PublicationUsersDiff<PostsMentionedUsersCrossRef>
    ) {
        insertMentioned(publicationUsersDiff.toInsert)
        deleteMentioned(publicationUsersDiff.toDelete)
    }

    @Query("DELETE FROM posts WHERE post_id = :id")
    suspend fun delete(id: Long)

}