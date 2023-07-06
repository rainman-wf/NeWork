package ru.rainman.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithJob(
    @Embedded
    val userEntity: UserEntity,

    @Relation(
        entity = JobEntity::class,
        parentColumn = "user_id",
        entityColumn = "employee_id"
    )
    val jobs: List<JobWithLink>
)