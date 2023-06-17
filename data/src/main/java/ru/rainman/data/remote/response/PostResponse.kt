package ru.rainman.data.remote.response

import com.google.gson.annotations.SerializedName

data class PostResponse(
    override val id: Long,
    override val authorId: Long,
    override val author: String,
    override val authorAvatar: String?,
    override val content: String,
    override val published: String,
    @SerializedName("coords")
    override val coordinates: Coordinates?,
    override val link: String?,
    override val likeOwnerIds: List<Long>,
    val mentionIds: List<Long>,
    val mentionedMe: Boolean,
    override val likedByMe: Boolean,
    override val attachment: Attachment?,
    override val ownedByMe: Boolean,
    override val users: Map<Long, UserPreviewResponse>,
) : PublicationResponse
