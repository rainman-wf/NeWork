package ru.rainman.data.impl.post

import ru.rainman.data.impl.PublicationUsersSyncUtil
import ru.rainman.data.local.dao.PostDao
import ru.rainman.data.local.entity.crossref.PostsLikeOwnersCrossRef
import ru.rainman.data.local.entity.crossref.PostsMentionedUsersCrossRef
import ru.rainman.data.remote.response.PostResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostUsersSyncUtil @Inject constructor(
    private val postDao: PostDao,
) : PublicationUsersSyncUtil() {

    suspend fun syncLikeOwners(posts: List<PostResponse>) {
        val newList = posts.map { post ->
            post.likeOwnerIds.map { userId ->
                PostsLikeOwnersCrossRef(
                    post.id, userId
                )
            }
        }.flatten()

        val oldList = postDao.getPostsLikeOwners(posts.map { it.id })
        postDao.updateLikeOwners(calcDiff(newList, oldList))
    }

    suspend fun syncLikeOwners(post: PostResponse) {
        val newList = post.likeOwnerIds.map { userId ->
            PostsLikeOwnersCrossRef(
                post.id, userId
            )
        }

        val oldList = postDao.getPostsLikeOwners(post.likeOwnerIds)
        postDao.updateLikeOwners(calcDiff(newList, oldList))
    }

    suspend fun syncMentioned(posts: List<PostResponse>) {
        val newList = posts.map { post ->
            post.mentionIds.map { userId ->
                PostsMentionedUsersCrossRef(
                    post.id, userId
                )
            }
        }.flatten()

        val oldList = postDao.getPostsMentioned(posts.map { it.id })
        postDao.updateMentioned(calcDiff(newList, oldList))
    }

    suspend fun syncMentioned(post: PostResponse) {
        val newList = post.mentionIds.map { userId ->
            PostsMentionedUsersCrossRef(
                post.id, userId
            )
        }

        val oldList = postDao.getPostsMentioned(post.mentionIds)
        postDao.updateMentioned(calcDiff(newList, oldList))
    }
}

