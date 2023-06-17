package ru.rainman.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.rainman.data.local.entity.FavoriteUserIdEntity
import ru.rainman.data.local.entity.JobEntity
import ru.rainman.data.local.entity.UserEntity
import ru.rainman.data.local.entity.UserWithJob

@Dao
interface UserDao : BaseDao<UserEntity> {

    @Transaction
    @Query("SELECT * FROM users ORDER BY user_id LIMIT :size OFFSET :offset")
    suspend fun getSized(offset: Int, size: Int): List<UserWithJob>

    @Transaction
    @Upsert
    suspend fun upsert(users: List<UserEntity>, jobs: List<JobEntity>)

    @Query("SELECT user_id FROM users")
    fun getAllIds(): Flow<List<Long>>

    @Transaction
    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<UserWithJob>>

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getById(userId: Long): UserWithJob?

    @Transaction
    @Query("SELECT * FROM users WHERE user_id IN (:userId)")
    suspend fun getByIds(userId: List<Long>): List<UserWithJob>

    @Query("SELECT user_id FROM users")
    suspend fun getIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setFavorite(userId: FavoriteUserIdEntity) : Long

    @Delete
    suspend fun resetFavorite(userId: FavoriteUserIdEntity) : Int

}