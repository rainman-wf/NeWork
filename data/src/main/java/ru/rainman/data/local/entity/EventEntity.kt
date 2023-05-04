package ru.rainman.data.local.entity

import androidx.room.*
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates

@Entity(tableName = "events")
internal data class EventEntity(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    val eventId: Long,
    @ColumnInfo("author_id")
    val authorId: Long,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded(prefix = "coordinates_")
    val coordinates: Coordinates?,
    val type: String,
    val link: String?,
    @ColumnInfo(name = "liked_by_me")
    val likedByMe: Boolean,
    @ColumnInfo(name = "participated_by_me")
    val participatedByMe: Boolean,
    @Embedded(prefix = "attachment_")
    val attachment: Attachment?,
    @ColumnInfo(name = "owned_by_me")
    val ownedByMe: Boolean,
)