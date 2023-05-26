package ru.rainman.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import ru.rainman.data.local.entity.crossref.PostsLikeOwnersCrossRef
import ru.rainman.data.local.entity.crossref.PostsMentionedUsersCrossRef

data class PostWithUsers(
    @Embedded
    val postEntity: PostEntity,
    @Relation(
        entity = UserEntity::class,
        parentColumn = "author_id",
        entityColumn = "user_id"
    )
    val author: UserWithJob,
    @Relation(
        entity = PostAttachmentEntity::class,
        parentColumn = "post_id",
        entityColumn = "post_id"
    )
    val attachment: PostAttachmentEntity?,
    @Relation(
        parentColumn = "post_id",
        entityColumn = "post_id"
    )
    val linkPreview: PostLinkPreviewEntity?,
    @Relation(
        entity = UserEntity::class,
        parentColumn = "post_id",
        entityColumn = "user_id",
        associateBy = Junction(PostsLikeOwnersCrossRef::class)
    )
    val likeOwners: List<UserWithJob>,
    @Relation(
        entity = UserEntity::class,
        parentColumn = "post_id",
        entityColumn = "user_id",
        associateBy = Junction(PostsMentionedUsersCrossRef::class)
    )
    val mentioned: List<UserWithJob>
)