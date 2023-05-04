package ru.rainman.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
internal data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: Long,
    val login: String,
    val name: String,
    val avatar: String?,
    @ColumnInfo(name = "job_id")
    val jobId: Long?
)

