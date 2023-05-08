package ru.rainman.data.local.entity

import androidx.room.*
import ru.rainman.domain.model.LinkPreview

@Entity(
    tableName = "post_links",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["post_id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class PostLinkPreviewEntity(
    @PrimaryKey
    @ColumnInfo(name = "post_id")
    override val publicationId: Long,
    @Embedded
    override val linkPreview: LinkPreview
) : PublicationLinkPreviewEntity








