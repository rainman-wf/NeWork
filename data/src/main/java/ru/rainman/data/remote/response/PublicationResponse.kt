package ru.rainman.data.remote.response

sealed interface PublicationResponse : LinkedResponse {
    val authorId: Long
    val author: String
    val authorAvatar: String?
    val content: String
    val published: String
    val coordinates: Coordinates?
    val likeOwnerIds: List<Long>
    val likedByMe: Boolean
    val attachment: Attachment?
    val ownedByMe: Boolean
    val users: Map<Long, UserPreviewResponse>
}

sealed interface BaseResponse {
    val id: Long
}

sealed interface LinkedResponse : BaseResponse {
    val link: String?
}