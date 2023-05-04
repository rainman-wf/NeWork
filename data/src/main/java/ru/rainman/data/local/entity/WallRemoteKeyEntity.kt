package ru.rainman.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "wall_remote_keys",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class WallRemoteKeyEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: Long,
    val max: Long? = null,
    val min: Long? = null
)
