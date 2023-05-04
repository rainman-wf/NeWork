package ru.rainman.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.rainman.data.local.entity.RemoteKeysEntity
import ru.rainman.data.local.entity.WallRemoteKeyEntity

@Dao
internal interface RemoteKeyDao {

    @Query("UPDATE remote_keys SET max = :value WHERE key = :key")
    fun setMax(value: Long, key: RemoteKeysEntity.Key)

    @Query("UPDATE remote_keys SET min = :value WHERE key = :key")
    fun setMin(value: Long, key: RemoteKeysEntity.Key)

    @Query("SELECT max FROM remote_keys WHERE key = :key")
    fun getMax(key: RemoteKeysEntity.Key): Long?

    @Query("SELECT min FROM remote_keys WHERE key = :key")
    fun getMin(key: RemoteKeysEntity.Key): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(remoteKeysEntity: RemoteKeysEntity)

    @Query("SELECT (max IS NULL AND min IS NULL) FROM remote_keys WHERE key = :key")
    fun isEmpty(key: RemoteKeysEntity.Key) :Boolean

    @Query("UPDATE wall_remote_keys SET max = :value WHERE user_id = :userId")
    fun setMax(value: Long, userId: Long)

    @Query("UPDATE wall_remote_keys SET min = :value WHERE user_id = :userId")
    fun setMin(value: Long, userId: Long)

    @Query("SELECT max FROM wall_remote_keys WHERE user_id = :userId")
    fun getMax(userId: Long): Long?

    @Query("SELECT min FROM wall_remote_keys WHERE user_id = :userId")
    fun getMin(userId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(wallRemoteKeyEntity: WallRemoteKeyEntity): Long

    @Query("SELECT (max IS NULL AND min IS NULL) FROM wall_remote_keys WHERE user_id = :userId")
    fun isEmpty(userId: Long) :Boolean

}