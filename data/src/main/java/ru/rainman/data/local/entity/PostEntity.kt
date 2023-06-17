package ru.rainman.data.local.entity

import androidx.room.*
import ru.rainman.domain.model.Coordinates

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    @ColumnInfo(name = "post_id")
    override val id: Long,
    @ColumnInfo(name = "author_id")
    override val authorId: Long,
    override val content: String,
    override val published: String,
    @Embedded(prefix = "coordinates_")
    val coordinates: Coordinates?,
    @ColumnInfo("attachment_key")
    override val attachmentKey: Long?,
    @ColumnInfo("link_key")
    override val linkKey: Long? = null,
    @ColumnInfo(name = "mentioned_me")
    val mentionedMe: Boolean,
    @ColumnInfo(name = "liked_by_me")
    override val likedByMe: Boolean,
    @ColumnInfo(name = "owned_by_me")
    override val ownedByMe: Boolean,
) : PublicationEntity