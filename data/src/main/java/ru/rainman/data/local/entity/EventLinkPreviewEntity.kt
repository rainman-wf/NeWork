package ru.rainman.data.local.entity

import androidx.room.*
import ru.rainman.domain.model.LinkPreview

@Entity(
    tableName = "event_links",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["event_id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventLinkPreviewEntity(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    override val publicationId: Long,
    @Embedded
    override val linkPreview: LinkPreview
) : PublicationLinkPreviewEntity