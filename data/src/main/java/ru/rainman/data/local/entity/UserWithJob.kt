package ru.rainman.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithJob(
    @Embedded
    val userEntity: UserEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id",
    )

    val userIdEntity: FavoriteUserIdEntity?,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "employee_id"
    )
    val jobs: List<JobEntity>
)