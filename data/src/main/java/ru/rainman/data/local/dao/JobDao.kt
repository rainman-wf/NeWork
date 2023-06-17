package ru.rainman.data.local.dao;

import androidx.room.Dao
import androidx.room.Query
import ru.rainman.data.local.entity.JobEntity

@Dao
interface JobDao : BaseDao<JobEntity> {

    @Query("SELECT * FROM jobs WHERE employee_id IN (:authorIds)")
    suspend fun getUsersJobs(authorIds: List<Long>): List<JobEntity>



}
