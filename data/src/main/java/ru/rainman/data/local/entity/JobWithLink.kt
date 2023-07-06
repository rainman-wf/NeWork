package ru.rainman.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation


data class JobWithLink(
    @Embedded
    val job: JobEntity,

    @Relation(parentColumn = "link_key", entityColumn = "key")
    val linkPreview: LinkPreviewEntity?,
)
