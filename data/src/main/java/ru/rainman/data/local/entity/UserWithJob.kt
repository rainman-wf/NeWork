package ru.rainman.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import ru.rainman.data.local.entity.FavoriteUserIdEntity
import ru.rainman.data.local.entity.JobEntity
import ru.rainman.data.local.entity.UserEntity

data class UserWithJob(
    @Embedded
    val userEntity: UserEntity,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id",
    )
    val userIdEntity: FavoriteUserIdEntity?,
    @Relation(
        parentColumn = "job_id",
        entityColumn = "id"
    )
    val job: JobEntity?
)