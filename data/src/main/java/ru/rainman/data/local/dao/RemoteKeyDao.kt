package ru.rainman.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.rainman.data.local.entity.RemoteKeysEntity

@Dao
interface RemoteKeyDao {

    @Query("UPDATE remote_keys SET max = :value WHERE `key` = :key")
    suspend fun setMax(key: Long, value: Long)

    @Query("UPDATE remote_keys SET min = :value WHERE `key` = :key")
    suspend fun setMin(key: Long, value: Long)

    @Query("SELECT max FROM remote_keys WHERE `key` = :key")
    suspend fun getMax(key: Long) : Long?

    @Query("SELECT min FROM remote_keys WHERE `key` = :key")
    suspend fun getMin(key: Long): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(remoteKeysEntity: RemoteKeysEntity)

}