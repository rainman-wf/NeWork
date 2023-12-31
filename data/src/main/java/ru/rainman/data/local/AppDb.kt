package ru.rainman.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.rainman.data.local.dao.*
import ru.rainman.data.local.dao.EventDao
import ru.rainman.data.local.dao.JobDao
import ru.rainman.data.local.dao.PostDao
import ru.rainman.data.local.dao.RemoteKeyDao
import ru.rainman.data.local.entity.*
import ru.rainman.data.local.entity.EventEntity
import ru.rainman.data.local.entity.LinkPreviewEntity
import ru.rainman.data.local.entity.JobEntity
import ru.rainman.data.local.entity.crossref.*

@Database(
    entities = [
        EventEntity::class,
        JobEntity::class,
        PostEntity::class,
        UserEntity::class,
        EventsLikeOwnersCrossRef::class,
        EventsSpeakersCrossRef::class,
        EventsParticipantsCrossRef::class,
        PostsLikeOwnersCrossRef::class,
        PostsMentionedUsersCrossRef::class,
        RemoteKeysEntity::class,
        LinkPreviewEntity::class,
        AttachmentEntity::class,
    ],
    version = 1
)
abstract class AppDb : RoomDatabase() {

    abstract val eventDao: EventDao
    abstract val jobDao: JobDao
    abstract val postDao: PostDao
    abstract val userDao: UserDao
    abstract val remoteKeyDao: RemoteKeyDao
}