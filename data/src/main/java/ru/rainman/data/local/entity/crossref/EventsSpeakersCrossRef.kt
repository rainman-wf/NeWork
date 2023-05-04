package ru.rainman.data.local.entity.crossref

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import ru.rainman.data.local.entity.EventEntity

@Entity(
    tableName = "events_speakers_cross_refs",
    primaryKeys = ["event_id", "user_id"],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["event_id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class EventsSpeakersCrossRef(
    @ColumnInfo(name = "event_id")  override val parentId: Long,
    @ColumnInfo(name = "user_id") override val childId: Long,
) : CrossRef

