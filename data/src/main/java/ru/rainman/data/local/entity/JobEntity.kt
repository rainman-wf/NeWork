package ru.rainman.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "jobs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["employee_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class JobEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "employee_id")
    val employeeId: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?,
)
