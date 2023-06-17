package ru.rainman.data.local.entity

import androidx.room.*
import ru.rainman.domain.model.LinkPreview

@Entity(tableName = "links")
data class LinkPreviewEntity(
    @PrimaryKey(autoGenerate = true)
    val key: Long = 0,
    @Embedded
    val linkPreview: LinkPreview
)