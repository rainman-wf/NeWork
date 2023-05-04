package ru.rainman.data.remote.response

import com.google.gson.annotations.SerializedName
import ru.rainman.data.remote.response.Attachment
import ru.rainman.data.remote.response.Coordinates

internal data class PostResponse(
    val id: Long,
    val authorId: Long,
    val content: String,
    val published: String,
    @SerializedName("coords")
    val coordinates: Coordinates?,
    val link: String?,
    val likeOwnerIds: List<Long>,
    val mentionIds: List<Long>,
    val mentionedMe: Boolean,
    val likedByMe: Boolean,
    val attachment: Attachment?,
    val ownedByMe: Boolean,
)
