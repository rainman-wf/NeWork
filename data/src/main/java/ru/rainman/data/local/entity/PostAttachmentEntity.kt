package ru.rainman.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "post_attachments",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["post_id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PostAttachmentEntity(
    @PrimaryKey
    @ColumnInfo(name = "post_id")
    override val publicationId: Long,
    override val url: String,
    override val type: AttachmentType,
    override val duration: Int?,
    override val ratio: Float?,
    override val artist: String?,
    override val title: String?
) : AttachmentEntity(publicationId, url, type, duration, ratio, artist, title)