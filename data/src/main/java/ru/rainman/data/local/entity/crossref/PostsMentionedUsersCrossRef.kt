package ru.rainman.data.local.entity.crossref

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import ru.rainman.data.local.entity.PostEntity

@Entity(
    tableName = "posts_mentioned_users_cross_refs",
    primaryKeys = ["post_id", "user_id"],
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["post_id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PostsMentionedUsersCrossRef(
    @ColumnInfo(name = "post_id")  override val parentId: Long,
    @ColumnInfo(name = "user_id") override val childId: Long,
) : CrossRef

