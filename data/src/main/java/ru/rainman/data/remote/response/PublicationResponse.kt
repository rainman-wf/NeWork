package ru.rainman.data.remote.response

sealed interface PublicationResponse {
    val id: Long
    val authorId: Long
    val author: String
    val authorAvatar: String?
    val content: String
    val published: String
    val coordinates: Coordinates?
    val link: String?
    val likeOwnerIds: List<Long>
    val likedByMe: Boolean
    val attachment: Attachment?
    val ownedByMe: Boolean
    val users: Map<Long, UserPreviewResponse>
}