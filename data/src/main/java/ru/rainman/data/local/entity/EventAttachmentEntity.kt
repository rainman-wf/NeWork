package ru.rainman.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_attachments",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["event_id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventAttachmentEntity(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    override val publicationId: Long,
    override val url: String,
    override val type: AttachmentType,
    override val duration: Int?,
    override val ratio: Float?,
    override val artist: String?,
    override val title: String?
) : AttachmentEntity(publicationId, url, type, duration, ratio, artist, title)

