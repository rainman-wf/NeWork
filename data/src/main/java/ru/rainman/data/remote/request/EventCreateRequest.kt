package ru.rainman.data.remote.request

import com.google.gson.annotations.SerializedName
import ru.rainman.data.remote.response.Attachment
import ru.rainman.data.remote.response.Coordinates

data class EventCreateRequest(
    val id: Long,
    val content: String?,
    @SerializedName("datetime")
    val dateTime: String?,
    @SerializedName("coords")
    val coordinates: Coordinates?,
    val type: String,
    val attachment: Attachment?,
    val link: String?,
    val speakerIds: List<Long>
)
