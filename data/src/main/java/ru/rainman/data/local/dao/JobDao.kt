package ru.rainman.data.local.dao;

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.rainman.data.local.entity.AttachmentEntity
import ru.rainman.data.local.entity.JobEntity
import ru.rainman.data.local.entity.JobWithLink
import ru.rainman.data.local.entity.LinkPreviewEntity

@Dao
interface JobDao : BaseDao<JobEntity> {


    @Query("SELECT * FROM jobs WHERE employee_id IN (:users)")
    suspend fun getJobs(users: List<Long>): List<JobEntity>

    @Transaction
    @Query("SELECT * FROM jobs WHERE employee_id = :userId")
    fun getUserJobs(userId: Long) : Flow<List<JobWithLink>>

    @Query("DELETE FROM links WHERE `key` = :key")
    suspend fun deleteLink(key: Long)

    @Query("SELECT url FROM links WHERE `key` = :key")
    suspend fun getLinkPreviewUrl(key: Long) : String?

    @Query("DELETE FROM jobs WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Transaction
    suspend fun insertLinkPreview(jobId: Long, link: LinkPreviewEntity) {
        val generatedKey = upsertLinkPreview(link)
        setLink(generatedKey, jobId)
    }

    @Transaction
    @Query("SELECT * FROM jobs WHERE id = :id")
    suspend fun getById(id: Long) : JobWithLink?

    @Upsert
    suspend fun upsertLinkPreview(link: LinkPreviewEntity) : Long

    @Query("UPDATE jobs SET link_key = :linkKey WHERE id = :jobId")
    suspend fun setLink(linkKey: Long, jobId: Long)

}
