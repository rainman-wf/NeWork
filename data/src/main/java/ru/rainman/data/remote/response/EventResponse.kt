package ru.rainman.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventResponse(
    override val id: Long,
    override val authorId: Long,
    override val author: String,
    override val authorAvatar: String?,
    val authorJob: String?,
    override val content: String,
    val datetime: String,
    override val published: String,
    @SerializedName("coords")
    override val coordinates: Coordinates?,
    val type: String,
    override val likeOwnerIds: List<Long>,
    override val likedByMe: Boolean,
    val speakerIds: List<Long>,
    val participantsIds: List<Long>,
    val participatedByMe: Boolean,
    override val attachment: Attachment?,
    override val link: String?,
    override val ownedByMe: Boolean,
    override val users: Map<Long, UserPreviewResponse>
) : PublicationResponse