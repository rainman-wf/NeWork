package ru.rainman.data.impl.post

import ru.rainman.data.impl.compareWith
import ru.rainman.data.impl.toEntity
import ru.rainman.data.local.dao.PostDao
import ru.rainman.data.local.entity.PostWithUsers
import ru.rainman.data.remote.response.PostResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostSyncUtil @Inject constructor(
    private val postDao: PostDao,
    private val postUsersSyncUtil: PostUsersSyncUtil
)  {

    suspend fun sync(postResponse: PostResponse): PostWithUsers {

        val newPost = postResponse.toEntity()
        val oldPost = postDao.getById(postResponse.id)

        if (oldPost!!.postEntity.compareWith(newPost)) postDao.update(newPost)

        postUsersSyncUtil.syncLikeOwners(postResponse)
        postUsersSyncUtil.syncMentioned(postResponse)

        return postDao.getById(postResponse.id)!!
    }
}