package ru.rainman.data.local.entity

import androidx.room.*
import ru.rainman.domain.model.Coordinates

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    override val id: Long,
    @ColumnInfo("author_id")
    override val authorId: Long,
    override val content: String,
    val datetime: String,
    override val published: String,
    @Embedded(prefix = "coordinates_")
    val coordinates: Coordinates?,
    @ColumnInfo("attachment_key")
    override val attachmentKey: Long? = null,
    val type: String,
    @ColumnInfo("link_key")
    override val linkKey: Long? = null,
    @ColumnInfo(name = "liked_by_me")
    override val likedByMe: Boolean,
    @ColumnInfo(name = "participated_by_me")
    val participatedByMe: Boolean,
    @ColumnInfo(name = "owned_by_me")
    override val ownedByMe: Boolean,
): PublicationEntity