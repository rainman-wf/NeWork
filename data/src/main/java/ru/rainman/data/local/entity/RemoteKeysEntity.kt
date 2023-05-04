package ru.rainman.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
internal data class RemoteKeysEntity(
    @PrimaryKey
    val key: Key,
    val max: Long?,
    val min: Long?
) {
    enum class Key {
        POSTS, EVENTS
    }
}
