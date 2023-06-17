package ru.rainman.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attachments")
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val key: Long = 0,
    val url: String,
    val type: AttachmentType,
    val duration: Int?,
    val ratio: Float?,
    val artist: String?,
    val title: String?
)