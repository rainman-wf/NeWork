package ru.rainman.data.local.entity

import androidx.room.*
import ru.rainman.domain.model.Coordinates

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    @ColumnInfo(name = "post_id")
    val postId: Long,
    @ColumnInfo(name = "author_id")
    val authorId: Long,
    val content: String,
    val published: String,
    @Embedded(prefix = "coordinates_")
    val coordinates: Coordinates?,
    val link: String?,
    @ColumnInfo(name = "mentioned_me")
    val mentionedMe: Boolean,
    @ColumnInfo(name = "liked_by_me")
    val likedByMe: Boolean,
    @ColumnInfo(name = "owned_by_me")
    val ownedByMe: Boolean,
)
