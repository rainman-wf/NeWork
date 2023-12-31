package ru.rainman.data.remote.request

import com.google.gson.annotations.SerializedName
import ru.rainman.data.remote.response.Attachment
import ru.rainman.data.remote.response.Coordinates

data class PostCreateRequest(
    val id: Long,
    val content: String,
    @SerializedName("coords")
    val coordinates: Coordinates?,
    val link: String?,
    val attachment: Attachment?,
    val mentionIds: List<Long> = emptyList()
)
